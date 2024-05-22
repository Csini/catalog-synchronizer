package hu.exercise.spring.kafka.input.tsv;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.init.CreateTopicsSpringApplication;
import jakarta.annotation.PostConstruct;

@Service
public class TSVHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSVHandler.class);

	private TsvParser parser;

	@Autowired
	private CustomBeanListProcessor customBeanListProcessor;

	@Autowired
	private CustomProcessorErrorHandler customProcessorErrorHandler;

	@Autowired
	public KafkaEnvironment environment;

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
		customProcessorErrorHandler.setContext(parser.getContext());
	}

	public void processInputFile() throws FileNotFoundException {

		// parses everything. All rows will be pumped into the Processor.
		parser.parse(TSVHandler.class.getResourceAsStream("/input/" + environment.getFilename()),
				StandardCharsets.UTF_8);

//		report.setCountReadedFromTsv(customBeanListProcessor.getCounter());
		long line = parser.getContext().currentLine();
		if (line >= 2) {
			// header row and it is after the last row
			line -= 2;
		}
		Report report = environment.getReport();
		report.setCountReadedFromTsv(line);
		report.setSumReaded(report.getSumReaded() + line);
		LOGGER.warn("readed valid from TSV: " + customBeanListProcessor.getCounter());
	}
}
