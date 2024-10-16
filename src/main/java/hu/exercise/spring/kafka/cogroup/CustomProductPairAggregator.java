package hu.exercise.spring.kafka.cogroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.KafkaUtils;
import hu.exercise.spring.kafka.topic.ProductEvent;

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

	private int flushSize;

	private int flushedItemCounter = 0;

	private int eventCounter = 0;

	private Timer.Context contextProcessing;

	private Timer timerProcessing;

	public CustomProductPairAggregator(MetricRegistry metrics, int aggregateWindowInSec, int flushSize,
			String stateStoreName, KafkaEnvironment environment) {
		super();
		this.stateStoreName = stateStoreName;
		this.environment = environment;
		this.aggregateWindowInSec = aggregateWindowInSec;
		this.flushSize = flushSize;
		this.timerProcessing = metrics.timer("timerProcessing");
	}

	@Override
	public void init(final ProcessorContext<String, ProductRollup> context) {
		this.context = context;
//		context.schedule(Duration.ofSeconds(240), PunctuationType.WALL_CLOCK_TIME, time -> flushStore());
//		context.schedule(Duration.ofSeconds(90), PunctuationType.STREAM_TIME, time -> flushStore());
//		context.schedule(Duration.ofSeconds(this.aggregateWindowInSec), PunctuationType.WALL_CLOCK_TIME, time -> flushStore());
//		context.schedule(Duration.ofMinutes(1), PunctuationType.STREAM_TIME, time -> flushStore());
		store = context.getStateStore(stateStoreName);

		this.contextProcessing = timerProcessing.time();

		processCounter = 0;
		flushCounter = 0;
		foundCounter = 0;
		flushedItemCounter = 0;
		eventCounter = 0;
	}

	@Override
	public void process(Record<String, ProductEvent> rec) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("processing: " + rec.value());
		}

		processCounter++;
//		LOGGER.info("processCounter: " + processCounter);

		String id = rec.value().getId();
		ProductPair oldValue = store.get(id);
//		LOGGER.warn("found(" + id + "): " + oldValue);

		if (oldValue == null) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("NEW!!!!");
			}
			oldValue = new ProductPair(id, rec.value().getRequestid().toString());
			eventCounter++;
		} else {
			foundCounter++;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("oldValue: " + oldValue);
		}

		ProductEvent productEvent = rec.value();

		final Source source = productEvent.getSource();

		if (Source.DB.equals(source)) {
			oldValue.setReadedFromDb(productEvent);
		} else if (Source.TSV.equals(source)) {
			oldValue.setReadedFromFile(productEvent);
		}

		store.put(id, oldValue);

		long toBeProcessed = environment.getReport().getSumToBeProcessed();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("processCounter: " + processCounter + " , toBeProcessed: " + toBeProcessed);
		}

		if (processCounter >= toBeProcessed) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("end - processCounter: " + processCounter + " , toBeProcessed: " + toBeProcessed);
			}
			environment.getReport().setSumEvent(eventCounter);
			if (flushSize <= 0) {
				flushStore();
			} else {
				flushStoreBatched();
			}
		}
	}

	private void flushStore() {
		LOGGER.info("flushStore: " + store.approximateNumEntries());
		if (flushCounter == 0 && store.approximateNumEntries() == 1) {
			LOGGER.info("don't do anything yet, waiting " + aggregateWindowInSec + " seconds...");
			return;
		}
		AtomicInteger counter = new AtomicInteger();
		try (final KeyValueIterator<String, ProductPair> it = store.all()) {

			ProductRollup toBeForwarded = new ProductRollup(environment.getRequestid().toString(), counter.get());
			while (it.hasNext()) {
				KeyValue<String, ProductPair> next = it.next();
				counter.incrementAndGet();
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("next: " + next);
				}
				toBeForwarded.getPairList().add(next.value);
				store.delete(next.key);
			}
			toBeForwarded.setProcessed(processCounter);
			LOGGER.info("flushed (" + flushCounter + "): " + counter);
			context.forward(new Record<String, ProductRollup>(environment.getRequestid() + "-" + flushCounter,
					toBeForwarded, new Date().getTime()));
			flushCounter++;

		}
	}

	private void flushStoreBatched() {
		LOGGER.info("flushStoreBatched: " + processCounter);
		;
		try (final KeyValueIterator<String, ProductPair> it = store.all()) {

			Stream<KeyValue<String, ProductPair>> stream = KafkaUtils.getStreamFromIterator(it);
					partitionStream(stream, flushSize).stream()
					.forEach(nextList -> {
						ProductRollup toBeForwarded = new ProductRollup(environment.getRequestid().toString(),
								flushCounter);
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("nextList: " + nextList);
						}
						nextList.stream().forEach(next -> {
							toBeForwarded.getPairList().add(next.value);
							flushedItemCounter++;
							store.delete(next.key);
						});
						LOGGER.info("flushed (" + flushCounter + ") sum: " + flushedItemCounter);
						toBeForwarded.setProcessed(flushedItemCounter);
						context.forward(new Record<String, ProductRollup>(
								environment.getRequestid() + "-" + flushCounter, toBeForwarded, new Date().getTime()));
						flushCounter++;
					});

		}
	}

	static <T> List<List<T>> partitionStream(Stream<T> source, int flushSize) {
		return source.collect(partitionBySize(flushSize, Collectors.toList()));
	}

	// https://www.baeldung.com/java-partition-stream
	static <T, A, R> Collector<T, ?, R> partitionBySize(int flushSize, Collector<List<T>, A, R> downstream) {
		Supplier<Accumulator<T, A>> supplier = () -> new Accumulator<>(flushSize, downstream.supplier().get(),
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
		private final int flushSize;
		private A downstreamAccumulator;
		private final BiConsumer<A, List<T>> batchFullListener;

		Accumulator(int flushSize, A accumulator, BiConsumer<A, List<T>> onBatchFull) {
			this.flushSize = flushSize;
			this.downstreamAccumulator = accumulator;
			this.batchFullListener = onBatchFull;
		}

		void add(T value) {
			values.add(value);
			if (values.size() == flushSize) {
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
		LOGGER.info("processCounter: " + processCounter);

		LOGGER.info("foundCounter: " + foundCounter);
		this.environment.getReport().setTimerProcessing(contextProcessing.stop() / 1_000_000_000.0);

		processCounter = 0;
		flushCounter = 0;
		foundCounter = 0;
		flushedItemCounter = 0;
		eventCounter = 0;
	}

}