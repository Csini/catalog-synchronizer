package hu.exercise.spring.kafka.input;

import java.util.HashMap;
import java.util.Map;

import org.grep4j.core.Grep4j;
import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResults;

public class GoogleProductCategoryValidator {
	
	private static final Map<String, Boolean> cache = new HashMap<>();

	private static Profile localProfile = ProfileBuilder.newBuilder().name("taxonomy-with-ids.en-US.txt").filePath(".")
			.onLocalhost().build();

	public static boolean isValid(String google_product_category) {
		if(cache.containsKey(google_product_category)) {
			return cache.get(google_product_category);
		}
		GrepResults results = Grep4j.grep(Grep4j.constantExpression(google_product_category), localProfile);
		boolean found = !results.isEmpty();
		cache.put(google_product_category, found);
		return found;
	}

}
