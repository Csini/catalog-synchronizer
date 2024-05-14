package hu.exercise.spring.kafka.cogroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = { "productrollupid", "id", "requestid" })
public class ProductRollup {

	private String productrollupid;

	private String id;

	private String requestid;

	private ProductPair pair;

	public ProductRollup(String productrollupid, String id, String requestid) {
		super();
		this.productrollupid = productrollupid;
		this.id = id;
		this.requestid = requestid;
		this.pair = new ProductPair(id, requestid);
	}

}
