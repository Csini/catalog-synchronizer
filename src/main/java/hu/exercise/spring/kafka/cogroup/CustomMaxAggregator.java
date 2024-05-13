package hu.exercise.spring.kafka.cogroup;

import java.time.Duration;
import java.util.Date;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;

public class CustomMaxAggregator implements Processor<String, ProductEvent, String, ProductRollup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomMaxAggregator.class);

	private ProcessorContext<String, ProductRollup> context;
	private KeyValueStore<String, ProductRollup> store;

	private final String stateStoreName;
	
	private int processCounter = 0;

	public CustomMaxAggregator(String stateStoreName) {
		super();
		this.stateStoreName = stateStoreName;
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
		
		LOGGER.info("processCounter: " + processCounter);

		ProductRollup oldValue = store.get(rec.key());

		if (oldValue == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NEW!!!!");
			}
			oldValue = new ProductRollup(rec.key());
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("oldValue: " + oldValue);
		}

		ProductEvent productEvent = rec.value();

		ProductPair pair = oldValue.getPair();

		final Source source = productEvent.getSource();

		if (Source.DB.equals(source)) {
			pair.setReadedFromDb(productEvent);
		} else if (Source.TSV.equals(source)) {
			pair.setReadedFromFile(productEvent);
		}

//		if (rec.timestamp() > oldValue.timestamp()) {
//			oldvalue.setTimestamp(rec.timestamp());
//			store.put(key, value);
//		}

		store.put(rec.key(), oldValue);
	}

	private void flushStore() {
		int counter = 0;
		final KeyValueIterator<String, ProductRollup> it = store.all();

		while (it.hasNext()) {
			counter++;
			final KeyValue<String, ProductRollup> next = it.next();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("next: " + next);
			}
			context.forward(new Record<String, ProductRollup>(next.key, next.value, new Date().getTime()));
			store.delete(next.key);
		}
		LOGGER.warn("flushed: " + counter);
		it.close();
	}

	@Override
	public void close() {
		// TODO clear store ?
//		store.
	}

}