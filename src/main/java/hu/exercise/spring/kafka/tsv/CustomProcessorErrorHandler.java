package hu.exercise.spring.kafka.tsv;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ProcessorErrorHandler;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.event.InvalidMessageProducer;
import hu.exercise.spring.kafka.topic.ProductErrorEvent;

@Service
public class CustomProcessorErrorHandler implements ProcessorErrorHandler<Context> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProcessorErrorHandler.class);

	@Autowired
	private InvalidMessageProducer invalidMessageProducer;

	@Autowired
	public KafkaEnvironment environment;

	private ParsingContext context;

	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, Context context) {
		LOGGER.error("Processing ERROR at line " + context.currentRecord() + " column " + context.currentColumn()
				+ " : " + error.getMessage());
		// send to invalid topic
		ProductErrorEvent productErrorEvent = new ProductErrorEvent(environment.getRequestid(),
				(inputRow == null || inputRow.length < 1) ? null : "" + Arrays.asList(inputRow), null, error);
		invalidMessageProducer.sendEvent(productErrorEvent);

	}

	public ParsingContext getContext() {
		return context;
	}

	public void setContext(ParsingContext context) {
		this.context = context;
	}

};
