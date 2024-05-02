package hu.exercise.spring.kafka.input.tsv;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import hu.exercise.spring.kafka.init.CreateTopicsSpringApplication;

public class TSVHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateTopicsSpringApplication.class);

	private static TsvParser parser;

	static {
		TsvParserSettings settings = new TsvParserSettings();

		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.setHeaderExtractionEnabled(true);
		//	settings.setNumberOfRowsToSkip(1);

		settings.setProcessor(new CustomBeanListProcessor());
		settings.setProcessorErrorHandler(new CustomProcessorErrorHandler());

		parser = new TsvParser(settings);
	}

	public static void processInputFile(String filename) throws FileNotFoundException {
		// parses everything. All rows will be pumped into the Processor.
		parser.parse(TSVHandler.class.getResourceAsStream(filename));
	}
}
