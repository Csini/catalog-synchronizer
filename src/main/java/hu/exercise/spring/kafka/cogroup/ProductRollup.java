package hu.exercise.spring.kafka.cogroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"productrollupid"})
public class ProductRollup {
	
	private String productrollupid;

	private ProductPair pair = new ProductPair();

	public ProductRollup(String productrollupid) {
		super();
		this.productrollupid = productrollupid;
	}

}
