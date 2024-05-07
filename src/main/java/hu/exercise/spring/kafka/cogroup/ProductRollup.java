package hu.exercise.spring.kafka.cogroup;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRollup {
	
	private UUID requestid;

	private String id;
	
	private ProductPair pair = new ProductPair();

	public ProductRollup(UUID requestid) {
		super();
		this.requestid = requestid;
	}

}
