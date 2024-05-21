package hu.exercise.spring.kafka.cogroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.KafkaUtils;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;

public class CustomProductPairAggregator implements Processor<String, ProductEvent, String, ProductRollup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProductPairAggregator.class);

	private ProcessorContext<String, ProductRollup> context;
	private KeyValueStore<String, ProductPair> store;

	private String stateStoreName;

	private int processCounter = 0;

	private int flushCounter = 0;

	private int foundCounter = 0;
	
	public KafkaEnvironment environment;
	
	private int aggregateWindowInSec;

	public CustomProductPairAggregator(int aggregateWindowInSec, String stateStoreName, KafkaEnvironment environment) {
		super();
		this.stateStoreName = stateStoreName;
		this.environment = environment;
		this.aggregateWindowInSec = aggregateWindowInSec;
	}

	@Override
	public void init(final ProcessorContext<String, ProductRollup> context) {
		this.context = context;
//		context.schedule(Duration.ofSeconds(240), PunctuationType.WALL_CLOCK_TIME, time -> flushStore());
//		context.schedule(Duration.ofSeconds(90), PunctuationType.STREAM_TIME, time -> flushStore());
//		context.schedule(Duration.ofSeconds(this.aggregateWindowInSec), PunctuationType.WALL_CLOCK_TIME, time -> flushStore());
//		context.schedule(Duration.ofSeconds(90), PunctuationType.STREAM_TIME, time -> flushStore());
		store = context.getStateStore(stateStoreName);
	}

	@Override
	public void process(Record<String, ProductEvent> rec) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processing: " + rec.value());
		}

		processCounter++;
//		LOGGER.info("processCounter: " + processCounter);

		String id = rec.value().getId();
		ProductPair oldValue = store.get(id);
//		LOGGER.warn("found(" + id + "): " + oldValue);

		if (oldValue == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NEW!!!!");
			}
			oldValue = new ProductPair(id, rec.value().getRequestid().toString());
		}else {
			foundCounter++;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("oldValue: " + oldValue);
		}

		ProductEvent productEvent = rec.value();

		final Source source = productEvent.getSource();

		if (Source.DB.equals(source)) {
			oldValue.setReadedFromDb(productEvent);
		} else if (Source.TSV.equals(source)) {
			oldValue.setReadedFromFile(productEvent);
		}

//		if (rec.timestamp() > oldValue.timestamp()) {
//			oldvalue.setTimestamp(rec.timestamp());
//			store.put(key, value);
//		}
//		LOGGER.warn("putting(" + id + "): " + oldValue);
		store.put(id, oldValue);
		
		if(processCounter>=environment.getReport().getSumReaded())
		{
			flushStore();
		}
	}

	private void flushStore() {
		LOGGER.warn("flushStore: " + store.approximateNumEntries());
		if(flushCounter==0 && store.approximateNumEntries()==1) {
			LOGGER.warn("don't do anything yet, waiting " + aggregateWindowInSec+ " seconds...");
			return;
		}
		AtomicInteger counter = new AtomicInteger();
		try (final KeyValueIterator<String, ProductPair> it = store.all()) {

			ProductRollup toBeForwarded = new ProductRollup(environment.getRequestid().toString(), counter.get(),
					processCounter);
			while (it.hasNext()) {
				KeyValue<String, ProductPair> next = it.next();
				counter.incrementAndGet();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("next: " + next);
				}
				toBeForwarded.getPairList().add(next.value);
				store.delete(next.key);
			}
			LOGGER.warn("flushed (" + flushCounter + "): " + counter);
			context.forward(new Record<String, ProductRollup>(environment.getRequestid() + "-" + flushCounter,
					toBeForwarded, new Date().getTime()));
			flushCounter++;

		}
	}

	@Deprecated
	// batchSize = 150, processCounter not working!
	private void flushStoreBatched(int batchSize) {
		try (final KeyValueIterator<String, ProductPair> it = store.all()) {

			Stream<KeyValue<String, ProductPair>> stream = KafkaUtils.getStreamFromIterator(it);
			partitionStream(stream, batchSize).forEach(nextList -> {
				AtomicInteger counter = new AtomicInteger();
				ProductRollup toBeForwarded = new ProductRollup(environment.getRequestid().toString(), flushCounter,
						processCounter);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("nextList: " + nextList);
				}
				nextList.stream().forEach(next -> {
					toBeForwarded.getPairList().add(next.value);
					counter.incrementAndGet();
					store.delete(next.key);
				});
				LOGGER.warn("flushed (" + flushCounter + "): " + counter);
				context.forward(new Record<String, ProductRollup>(environment.getRequestid() + "-" + flushCounter,
						toBeForwarded, new Date().getTime()));
				flushCounter++;
			});

		}
	}

	static <T> List<List<T>> partitionStream(Stream<T> source, int batchSize) {
		return source.collect(partitionBySize(batchSize, Collectors.toList()));
	}

	// https://www.baeldung.com/java-partition-stream
	static <T, A, R> Collector<T, ?, R> partitionBySize(int batchSize, Collector<List<T>, A, R> downstream) {
		Supplier<Accumulator<T, A>> supplier = () -> new Accumulator<>(batchSize, downstream.supplier().get(),
				downstream.accumulator()::accept);

		BiConsumer<Accumulator<T, A>, T> accumulator = (acc, value) -> acc.add(value);

		BinaryOperator<Accumulator<T, A>> combiner = (acc1, acc2) -> acc1.combine(acc2, downstream.combiner());

		Function<Accumulator<T, A>, R> finisher = acc -> {
			if (!acc.values.isEmpty()) {
				downstream.accumulator().accept(acc.downstreamAccumulator, acc.values);
			}
			return downstream.finisher().apply(acc.downstreamAccumulator);
		};

		return Collector.of(supplier, accumulator, combiner, finisher, Collector.Characteristics.UNORDERED);
	}

	static class Accumulator<T, A> {
		private final List<T> values = new ArrayList<>();
		private final int batchSize;
		private A downstreamAccumulator;
		private final BiConsumer<A, List<T>> batchFullListener;

		Accumulator(int batchSize, A accumulator, BiConsumer<A, List<T>> onBatchFull) {
			this.batchSize = batchSize;
			this.downstreamAccumulator = accumulator;
			this.batchFullListener = onBatchFull;
		}

		void add(T value) {
			values.add(value);
			if (values.size() == batchSize) {
				batchFullListener.accept(downstreamAccumulator, new ArrayList<>(values));
				values.clear();
			}
		}

		Accumulator<T, A> combine(Accumulator<T, A> other, BinaryOperator<A> accumulatorCombiner) {
			this.downstreamAccumulator = accumulatorCombiner.apply(downstreamAccumulator, other.downstreamAccumulator);
			other.values.forEach(this::add);
			return this;
		}
	}

	@Override
	public void close() {
		// TODO clear store ?
//		store.
		LOGGER.info("processCounter: " + processCounter);
		
		LOGGER.warn("foundCounter: " + foundCounter);
	}

}