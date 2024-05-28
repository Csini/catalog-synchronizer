package hu.exercise.spring.kafka.input;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleProductCategoryValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleProductCategoryValidator.class);

	private static final Map<String, Boolean> cache = new HashMap<>();

	public static boolean isValid(String google_product_category) throws IOException, URISyntaxException {
		if (cache.containsKey(google_product_category)) {
			return cache.get(google_product_category);
		}
		try (Stream<String> stream = Files.lines(
				Path.of(GoogleProductCategoryValidator.class.getResource("/taxonomy-with-ids.en-US.txt").toURI()))) {

			boolean found = stream.filter(str -> str.startsWith(google_product_category + " - ")).findFirst()
					.isPresent();

			cache.put(google_product_category, found);
			return found;

		}
	}

}
