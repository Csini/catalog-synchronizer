package hu.exercise.spring.kafka.tsv;

import java.util.ArrayList;
import java.util.List;
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
import hu.exercise.spring.kafka.event.ValidMessageProducer;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.input.ProductValidator;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import jakarta.validation.ConstraintViolation;

@Service
public class CustomBeanListProcessor extends BeanListProcessor<Product> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomBeanListProcessor.class);

	@Autowired
	ProductValidator productValidator;

	@Autowired
	private ValidMessageProducer validMessageProducer;
	
	@Autowired
	private InvalidMessageProducer invalidMessageProducer;

	@Autowired
	public KafkaEnvironment environment;

	private int counter = 0;

	private List<Product> validFromTsv = new ArrayList<>();
	
	public CustomBeanListProcessor() {
		super(Product.class);
	}

	@Override
	public void beanProcessed(Product bean, ParsingContext context) {

		bean.setRun(environment.getRun());

		Set<ConstraintViolation<Product>> violations = productValidator.validate(bean);

		counter++;

		if (violations.isEmpty()) {
			
			validFromTsv.add(bean);

		} else {
			// send to invalid topic
			String violationtext = violations.stream()
					.map(v -> v.getPropertyPath() + ": " + v.getInvalidValue() + " - " + v.getMessage())
					.collect(Collectors.joining(","));
			LOGGER.error("at " + bean.getId() + ": " + violationtext);
			ProductErrorEvent productErrorEvent = new ProductErrorEvent(environment.getRequestid(), bean.getId(), bean,
					new IllegalArgumentException(violationtext));
			invalidMessageProducer.sendEvent(productErrorEvent);

		}
	}

	public int getCounter() {
		return counter;
	}

	@Override
	public void processEnded(ParsingContext context) {
		super.processEnded(context);
		environment.getReport().setCountReadedFromTsvValid(validFromTsv.size());

		@NonNull
		Observable<Product> published = Observable.fromIterable(validFromTsv);

		// send to valid topic
		validMessageProducer.sendEvent(published);
//		productMessageProducer.sendMessages(published, Source.TSV);
		
//		published.connect();
		
	}
}
