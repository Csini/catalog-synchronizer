package hu.exercise.spring.kafka.input.tsv;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.InvalidExample;
import hu.exercise.spring.kafka.input.Product;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Service
public class InvalidExamplesHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvalidExamplesHandler.class);

	private TsvWriter writer;

//	@Autowired
//	private CustomProcessorErrorHandler customWriterErrorHandler;

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();

	@Autowired
	public KafkaEnvironment environment;

	private EasyRandom generator;

	@PostConstruct
	private void postConstruct() {

		TsvWriterSettings settings = new TsvWriterSettings();
		settings.setRowWriterProcessor(new BeanWriterProcessor<Product>(Product.class));
//		settings.setProcessorErrorHandler(customWriterErrorHandler);
		writer = new TsvWriter(settings);
		// customWriterErrorHandler.setContext(writer.getContext());
		EasyRandomParameters parameters = new EasyRandomParameters();
		parameters.excludeType((Class<?> c) -> Currency.class.equals(c));
//		image_link
		parameters.randomize(field -> {

			return Math.random() > 0.5 && ("link".equals(field.getName()) || ("image_link".equals(field.getName())));
		}, () -> "http://random");
		
		parameters.randomize(field -> {

			return Math.random() > 0.5 && "id".equals(field.getName());
		}, () -> "12345678910111213141516171819202122232425262728293031323334353637383940");
		generator = new EasyRandom(parameters);
	}

	public List<InvalidExample> getInvalidExamples(int count) {

		LOGGER.warn("generating Invalid Examples...");
		List<InvalidExample> ret = new ArrayList<>();

		while (ret.size() < count - 1) {
			Product product = generator.nextObject(Product.class);
			InvalidExample filled = fillInvalidExample(product);
			if (filled != null) {
				ret.add(filled);
			}

		}

		writer.close();
		return ret;
	}

	private InvalidExample fillInvalidExample(Product product) {
		Set<ConstraintViolation<Product>> violations = validator.validate(product);
		if (violations.isEmpty()) {
			// valid
			return null;
		}
		String tsvContent = writer.processRecordToString(product);
		InvalidExample invalidExample = new InvalidExample(tsvContent, product, violations);
		return invalidExample;
	}
}
