package hu.exercise.spring.kafka.input.tsv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ProcessorErrorHandler;

@Service
public class CustomProcessorErrorHandler implements ProcessorErrorHandler<Context> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomProcessorErrorHandler.class);

	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, Context context) {
		LOGGER.error("Processing ERROR at line " + context.currentRecord() + " column " + context.currentColumn() +" : " + error.getMessage());
		// TODO send to invalid topic

	}

};
