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
public class ProductErrorEvent {

	private UUID requestid;
	private String id;

	public String getErrorMessage() {
		if (error == null) {
			return null;
		}
		return this.error.getMessage();
	}

	private Product product;
	private Throwable error;
}
