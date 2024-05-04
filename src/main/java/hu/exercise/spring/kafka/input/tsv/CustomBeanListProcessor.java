package hu.exercise.spring.kafka.input.tsv;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.BeanListProcessor;

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
	
//	@Autowired
//	private ProductRepository repository;
	
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

			// TODO send to valid topic
//			repository.save(bean);

		} else {
			LOGGER.error("at " + bean.getId(), violations);
			// TODO send to invalid topic

//			for (ConstraintViolation<Product> violation : violations) {
//				LOGGER.error(violation.getMessage());
//			}

		}
	}

}
