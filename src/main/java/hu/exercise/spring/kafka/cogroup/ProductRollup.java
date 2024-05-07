package hu.exercise.spring.kafka.cogroup;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ProductRollup {
	
	private String id;
	
//	private Map<String, ProductPair> products = new HashMap<>();
	
	private ProductPair pair = new ProductPair();

}
