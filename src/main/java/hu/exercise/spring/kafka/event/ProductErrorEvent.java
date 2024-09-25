package hu.exercise.spring.kafka.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class ProductErrorEvent {

	@KafkaEntityKey
	@Schema(description = "Run's unique identifier", example = "a3dbaa5a-1375-491e-8c21-403864de8779")
	private UUID requestid;
	
	@Schema(description = "Your product’s unique identifier", example = "A2B4")
	private String id;

	public String getErrorMessage() {
		if (error == null) {
			return null;
		}
		return this.error.getMessage();
	}

	private Product product;

	private Throwable error;

	@JsonIgnore
	public Throwable getError() {
		return error;
	}
	
}
