package hu.exercise.spring.kafka.cogroup;

import hu.exercise.spring.kafka.event.ProductEvent;
import lombok.Data;

@Data
public class ProductPair {

	public Action getAction() {
		if (readedFromDb==null || readedFromDb.getSource() == null) {
			return Action.INSERT;
		} else if (readedFromFile==null || readedFromFile.getSource() == null) {
			return Action.DELETE;
		}
		return Action.UPDATE;
	}

	private ProductEvent readedFromFile;
	
	private ProductEvent readedFromDb;
}