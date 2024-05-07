package hu.exercise.spring.kafka.cogroup;

import hu.exercise.spring.kafka.input.Product;
import lombok.Data;

@Data
public class ProductPair {

	private Product readedFromFile;

	private Product readedFromDb;

	public Action getAction() {
		if (readedFromDb == null) {
			return Action.INSERT;
		} else if (readedFromFile == null) {
			return Action.DELETE;
		}
		return Action.UPDATE;
	}

}