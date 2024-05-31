package hu.exercise.spring.kafka.input;

import java.math.BigDecimal;
import java.util.Currency;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductIsChangedTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductIsChangedTest.class);

	private EasyRandom generator = new EasyRandom();

	@Test
	public void test_product_isChanged_true() {
		Product product = new Product();
		product.setId("test");
		String nextObject = generator.nextObject(String.class);
		product.setTitle(nextObject);
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		double nextDouble = generator.nextDouble(20_000);
		price.setAmount(BigDecimal.valueOf(nextDouble));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(nextObject);
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		
		
		Product productNew = new Product();
		//change
		productNew.setId("test_with_intension_change");
		
		productNew.setTitle(nextObject);
		productNew.setLink("http://test");
		MonetaryAmount priceNew = new MonetaryAmount();
		priceNew.setAmount(BigDecimal.valueOf(nextDouble));
		priceNew.setCurrency(Currency.getInstance("EUR"));
		productNew.setPrice(price);
		productNew.setDescription(nextObject);
		productNew.setImage_link("http://image_link_test");
		productNew.setAvailability(Availability.IN_STOCK);
		
		product.merge(productNew);
		
		Assertions.assertTrue(product.isChange());
	}
	
	@Test
	public void test_product_isChanged_false() {
		Product product = new Product();
		product.setId("test");
		String nextObject = generator.nextObject(String.class);
		product.setTitle(nextObject);
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		double nextDouble = generator.nextDouble(20_000);
		price.setAmount(BigDecimal.valueOf(nextDouble));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(nextObject);
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		
		
		Product productNew = new Product();
		// NO change
		productNew.setId("test");
		
		productNew.setTitle(nextObject);
		productNew.setLink("http://test");
		MonetaryAmount priceNew = new MonetaryAmount();
		priceNew.setAmount(BigDecimal.valueOf(nextDouble));
		priceNew.setCurrency(Currency.getInstance("EUR"));
		productNew.setPrice(price);
		productNew.setDescription(nextObject);
		productNew.setImage_link("http://image_link_test");
		productNew.setAvailability(Availability.IN_STOCK);
		
		product.merge(productNew);
		
		Assertions.assertFalse(product.isChange());
	}
	
	@Test
	public void test_product_isChanged_merge_null_true() {
		Product product = new Product();
		product.setId("test");
		String nextObject = generator.nextObject(String.class);
		product.setTitle(nextObject);
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		double nextDouble = generator.nextDouble(20_000);
		price.setAmount(BigDecimal.valueOf(nextDouble));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(nextObject);
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		product.setBrand("blablabla");
		
		
		Product productNew = new Product();
		productNew.setId("test");
		
		productNew.setTitle(nextObject);
		productNew.setLink("http://test");
		MonetaryAmount priceNew = new MonetaryAmount();
		priceNew.setAmount(BigDecimal.valueOf(nextDouble));
		priceNew.setCurrency(Currency.getInstance("EUR"));
		productNew.setPrice(price);
		productNew.setDescription(nextObject);
		productNew.setImage_link("http://image_link_test");
		productNew.setAvailability(Availability.IN_STOCK);
		// change
		productNew.setBrand(null);
		
		product.merge(productNew);
		
		Assertions.assertTrue(product.isChange());
	}
	
	@Test
	public void test_product_isChanged_null_merge_true() {
		Product product = new Product();
		product.setId("test");
		String nextObject = generator.nextObject(String.class);
		product.setTitle(nextObject);
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		double nextDouble = generator.nextDouble(20_000);
		price.setAmount(BigDecimal.valueOf(nextDouble));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(nextObject);
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		product.setBrand(null);
		
		Product productNew = new Product();
		productNew.setId("test");
		
		productNew.setTitle(nextObject);
		productNew.setLink("http://test");
		MonetaryAmount priceNew = new MonetaryAmount();
		priceNew.setAmount(BigDecimal.valueOf(nextDouble));
		priceNew.setCurrency(Currency.getInstance("EUR"));
		productNew.setPrice(price);
		productNew.setDescription(nextObject);
		productNew.setImage_link("http://image_link_test");
		productNew.setAvailability(Availability.IN_STOCK);
		// change
		productNew.setBrand("testbrand");
		
		product.merge(productNew);
		
		Assertions.assertTrue(product.isChange());
	}
}
