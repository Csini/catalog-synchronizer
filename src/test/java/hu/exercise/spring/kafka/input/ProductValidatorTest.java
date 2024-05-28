package hu.exercise.spring.kafka.input;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;

public class ProductValidatorTest extends ProductValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductValidatorTest.class);

	private EasyRandom generator = new EasyRandom();

	@Test
	public void test_product_valid() {
		Product product = new Product();
		product.setId("test");
		product.setTitle(generator.nextObject(String.class));
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		price.setAmount(BigDecimal.valueOf(generator.nextDouble(20_000)));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(generator.nextObject(String.class));
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);

		LOGGER.info(product.toString());
		Set<ConstraintViolation<Product>> violations = validate(product);

		violations.forEach(v -> LOGGER.info(v.toString()));

		Assertions.assertEquals(0, violations.size());
	}

	@Test
	public void test_product_new_brand_required() {
		Product product = new Product();
		product.setId("test");
		product.setTitle(generator.nextObject(String.class));
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		price.setAmount(BigDecimal.valueOf(generator.nextDouble(20_000)));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(generator.nextObject(String.class));
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);

		product.setCondition(Condition.NEW);

		LOGGER.info(product.toString());
		Set<ConstraintViolation<Product>> violations = validate(product);

		violations.forEach(v -> LOGGER.info(v.toString()));

		Assertions.assertEquals(1, violations.size());
		Assertions.assertEquals("brand is required for new products", violations.iterator().next().getMessage());
	}

	@Test
	public void test_product_google_product_category_166() {
		Product product = new Product();
		product.setId("test");
		product.setTitle(generator.nextObject(String.class));
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		price.setAmount(BigDecimal.valueOf(generator.nextDouble(20_000)));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(generator.nextObject(String.class));
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		product.setGoogle_product_category("166");
		product.setAge_group(null);

		LOGGER.info(product.toString());
		Set<ConstraintViolation<Product>> violations = validate(product);

		violations.forEach(v -> LOGGER.info(v.toString()));

		Assertions.assertEquals(1, violations.size());
		Assertions.assertEquals("age_group is required for Apparel & Accessories products", violations.iterator().next().getMessage());
	}
	
	@Test
	public void test_product_google_product_category_12345_invalid() {
		Product product = new Product();
		product.setId("test");
		product.setTitle(generator.nextObject(String.class));
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		price.setAmount(BigDecimal.valueOf(generator.nextDouble(20_000)));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(generator.nextObject(String.class));
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		product.setGoogle_product_category("12345");
		product.setAge_group(null);

		LOGGER.info(product.toString());
		Set<ConstraintViolation<Product>> violations = validate(product);

		violations.forEach(v -> LOGGER.info(v.toString()));

		Assertions.assertEquals(1, violations.size());
		Assertions.assertEquals("google_product_category should be in the list", violations.iterator().next().getMessage());
	}

}
