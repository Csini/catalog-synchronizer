package hu.exercise.spring.kafka.cogroup;

import java.util.HashSet;
import java.util.Set;

import hu.exercise.spring.kafka.input.Product;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of="tsvContent")
@AllArgsConstructor
public class InvalidExample {

	private String tsvContent;

	private Product product;
	
	private Set<ConstraintViolation<Product>> violations = new HashSet<>();

}
