package hu.exercise.spring.kafka.input.tsv;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import hu.exercise.spring.kafka.init.CreateTopicsSpringApplication;
import jakarta.annotation.PostConstruct;

@Service
public class TSVHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateTopicsSpringApplication.class);

	private static TsvParser parser;
	
	@Autowired
	private CustomBeanListProcessor customBeanListProcessor;
	
	@Autowired
	private CustomProcessorErrorHandler customProcessorErrorHandler;

	@PostConstruct
	private void postConstruct() {
		TsvParserSettings settings = new TsvParserSettings();

		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.setHeaderExtractionEnabled(true);
		// settings.setNumberOfRowsToSkip(1);

		settings.setProcessor(customBeanListProcessor);
		settings.setProcessorErrorHandler(customProcessorErrorHandler);

		parser = new TsvParser(settings);
	}

	public void processInputFile(String filename) throws FileNotFoundException {
		
		customBeanListProcessor.setFilename(filename);
		
		// parses everything. All rows will be pumped into the Processor.
		parser.parse(TSVHandler.class.getResourceAsStream(filename));
	}
}
