package hu.exercise.spring.kafka.input;

import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Service
public class ProductValidator {

	private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private Validator validator = factory.getValidator();
	
	public Set<ConstraintViolation<Product>> validate(Product bean) {
		return validator.validate(bean);
	}
}
