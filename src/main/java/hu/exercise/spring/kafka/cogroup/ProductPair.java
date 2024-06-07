package hu.exercise.spring.kafka.cogroup;

import org.springframework.util.Assert;

import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.input.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "requestid" })
public class ProductPair {

	private String id;

	private String requestid;

	public Action getAction() {
		if (getReadedFromDb() == null) {
			return Action.INSERT;
		}
		if (getReadedFromFile() == null) {
			return Action.DELETE;
		}
		if (getReadedFromDb() != null && getReadedFromFile() != null) {
			return Action.UPDATE;
		}
		return Action.ERROR;
	}

	private ProductEvent readedFromFile;

	private ProductEvent readedFromDb;

	public Product getProductToSave() {
		Product product = null;
		if (Action.INSERT.equals(getAction())) {
			product = getReadedFromFile().getProduct();
		} else if (Action.UPDATE.equals(getAction())) {
			product = getReadedFromFile().getProduct();
		} else if (Action.DELETE.equals(getAction())) {
			product = getReadedFromDb().getProduct();
		}
		Assert.notNull(product, "Product must not be null - " + getRequestid());
		return product;
	}

	public ProductPair(String id, String requestid) {
		super();
		this.id = id;
		this.requestid = requestid;
	}

}