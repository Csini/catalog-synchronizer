package hu.exercise.spring.kafka.event;

import hu.exercise.spring.kafka.input.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductErrorEvent {

	private String id;
	private Product product;
	private Throwable error;
}
