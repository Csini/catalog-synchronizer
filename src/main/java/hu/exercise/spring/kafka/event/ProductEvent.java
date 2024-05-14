package hu.exercise.spring.kafka.event;

import java.util.UUID;

import hu.exercise.spring.kafka.input.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "requestid" })
public class ProductEvent {
	
	private String id;
	
	private UUID requestid;
	
	private Source source;
	
	private Product product;

}
