package hu.exercise.spring.kafka.input.tsv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ProcessorErrorHandler;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.event.InvalidMessageProducer;
import hu.exercise.spring.kafka.event.ProductErrorEvent;

@Service
public class CustomProcessorErrorHandler implements ProcessorErrorHandler<Context> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProcessorErrorHandler.class);

	@Autowired
	private InvalidMessageProducer invalidMessageProducer;

	@Autowired
	public KafkaEnvironment environment;

	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, Context context) {
		LOGGER.error("Processing ERROR at line " + context.currentRecord() + " column " + context.currentColumn()
				+ " : " + error.getMessage());
		// send to invalid topic
		// TODO
		ProductErrorEvent productErrorEvent = new ProductErrorEvent(environment.getRequestid(),
				(inputRow == null || inputRow.length < 1) ? null : "" + inputRow[0], null, error);
		invalidMessageProducer.sendEvent(productErrorEvent);

	}

};
