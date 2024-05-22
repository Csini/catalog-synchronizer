package hu.exercise.spring.kafka.input.tsv;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.BeanListProcessor;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.event.InvalidMessageProducer;
import hu.exercise.spring.kafka.event.ProductErrorEvent;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.ProductEventMessageProducer;
import hu.exercise.spring.kafka.event.Source;
import hu.exercise.spring.kafka.event.ValidMessageProducer;
import hu.exercise.spring.kafka.input.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Service
public class CustomBeanListProcessor extends BeanListProcessor<Product> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomBeanListProcessor.class);

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();

//	@Autowired
//	private ProductRepository repository;

	@Autowired
	private ValidMessageProducer validMessageProducer;

	@Autowired
	private InvalidMessageProducer invalidMessageProducer;

	@Autowired
	public KafkaEnvironment environment;

	private int counter = 0;

	public CustomBeanListProcessor() {
		super(Product.class);
	}

//	@Override
//	public Product createBean(String[] row, Context context) {
//		try {
//			return super.createBean(row, context);
//		} catch (Exception e) {
//			LOGGER.error("at " + row, e);
//			// TODO send to invalid topic
//
//			// TODO
//			return null;
//		}
//	}

	@Override
	public void beanProcessed(Product bean, ParsingContext context) {

		// TODO
		// LOGGER.info(bean.toString());

		bean.setRun(environment.getRun());

		Set<ConstraintViolation<Product>> violations = validator.validate(bean);

		counter++;

		if (violations.isEmpty()) {

//			repository.save(bean);

			// send to valid topic
			ProductEvent productEvent = new ProductEvent(bean.getId(), environment.getRequestid(), Source.TSV, bean);
			validMessageProducer.sendEvent(productEvent);

		} else {
			LOGGER.error("at " + bean.getId(), violations);
			// send to invalid topic
			// TODO
			String violationtext = violations.stream()
					.map(v -> v.getPropertyPath() + ": " + v.getInvalidValue() + " - " + v.getMessage())
					.collect(Collectors.joining(","));
			ProductErrorEvent productErrorEvent = new ProductErrorEvent(environment.getRequestid(), bean.getId(), bean,
					new IllegalArgumentException(violationtext));
			invalidMessageProducer.sendEvent(productErrorEvent);

//			for (ConstraintViolation<Product> violation : violations) {
//				LOGGER.error(violation.getMessage());
//			}

		}
	}

	public int getCounter() {
		return counter;
	}

}
