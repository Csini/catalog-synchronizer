package hu.exercise.spring.kafka.cogroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;

public class CustomProductPairAggregator implements Processor<String, ProductEvent, String, ProductRollup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProductPairAggregator.class);

	private ProcessorContext<String, ProductRollup> context;
	private KeyValueStore<String, ProductPair> store;

	private String stateStoreName;

	private int processCounter = 0;

	private int flushCounter = 0;

	public KafkaEnvironment environment;

	public CustomProductPairAggregator(String stateStoreName, KafkaEnvironment environment) {
		super();
		this.stateStoreName = stateStoreName;
		this.environment = environment;
	}

	@Override
	public void init(final ProcessorContext<String, ProductRollup> context) {
		this.context = context;
		context.schedule(Duration.ofSeconds(60), PunctuationType.WALL_CLOCK_TIME, time -> flushStore());
		context.schedule(Duration.ofSeconds(10), PunctuationType.STREAM_TIME, time -> flushStore());
		store = context.getStateStore(stateStoreName);
	}

	@Override
	public void process(Record<String, ProductEvent> rec) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processing: " + rec.value());
		}

		processCounter++;

//		LOGGER.info("processCounter: " + processCounter);

		ProductPair oldValue = store.get(rec.key());

		if (oldValue == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NEW!!!!");
			}
			oldValue = new ProductPair(rec.value().getId(), rec.value().getRequestid().toString());
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

		store.put(rec.key(), oldValue);
	}

	private void flushStore() {
		AtomicInteger counter = new AtomicInteger();
		try (final KeyValueIterator<String, ProductPair> it = store.all()) {

			ProductRollup toBeForwarded = new ProductRollup(environment.getRequestid().toString(), counter.get(), processCounter);
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

	@Override
	public void close() {
		// TODO clear store ?
//		store.
		LOGGER.info("processCounter: " + processCounter);
	}

}