package hu.exercise.spring.kafka.topic;

import hu.exercise.spring.kafka.input.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.csini.spring.kafka.KafkaEntity;
import net.csini.spring.kafka.KafkaEntityKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "requestid" })
@KafkaEntity
public class ValidProductEvent {
	
	@Schema(description = "Your product’s unique identifier", example = "A2B4")
	private String id;
	
	@KafkaEntityKey
	@Schema(description = "Run's unique identifier", example = "a3dbaa5a-1375-491e-8c21-403864de8779")
	private String requestid;
	
	private Product product;

}
