package hu.exercise.spring.kafka.input.tsv;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.BeanListProcessor;

import hu.exercise.spring.kafka.event.ProductErrorEvent;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.repository.ProductRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Service
public class CustomBeanListProcessor extends BeanListProcessor<Product> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomBeanListProcessor.class);

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();

	@Autowired
	public NewTopic validProduct;
	
	@Autowired
	public NewTopic invalidProduct;

//	@Autowired
//	private ProductRepository repository;

	@Autowired
	private KafkaTemplate<String, ProductEvent> validFromTSVKafkaTemplate;

	@Autowired
	private KafkaTemplate<String, ProductErrorEvent> invalidFromTSVKafkaTemplate;

	private String filename;

	public CustomBeanListProcessor() {
		super(Product.class);
	}

	public void setFilename(String filename) {
		this.filename = filename;
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
		//LOGGER.info(bean.toString());

		bean.setFilename(filename);
		
		Set<ConstraintViolation<Product>> violations = validator.validate(bean);

		if (violations.isEmpty()) {

//			repository.save(bean);
			
			//  send to valid topic
			validFromTSVKafkaTemplate.send(validProduct.name(), bean.getId(),new ProductEvent(Source.TSV, bean));

		} else {
			LOGGER.error("at " + bean.getId(), violations);
			//  send to invalid topic
			//TODO
			String violationtext = violations.stream().map(v -> v.toString()).collect(Collectors.joining(","));
			invalidFromTSVKafkaTemplate.send(invalidProduct.name(), new ProductErrorEvent(bean.getId(), bean, new IllegalArgumentException(violationtext)));

//			for (ConstraintViolation<Product> violation : violations) {
//				LOGGER.error(violation.getMessage());
//			}

		}
	}

}
