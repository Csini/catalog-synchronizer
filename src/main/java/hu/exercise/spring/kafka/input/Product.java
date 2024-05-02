package hu.exercise.spring.kafka.input;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.EnumOptions;
import com.univocity.parsers.annotations.Parsed;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * {@link} https://support.google.com/merchants/answer/7052112?hl=en#zippy=%2Cother-requirements%2Cformatting-your-product-data
 * 
 * @author csini
 *
 */
@Data
public class Product {

	@Schema(name = "Your product’s unique identifier", example = "A2B4")
	@NotNull(message = "id is required")
	@Size(max = 50, min = 1, message = "id max 50 character")
	// Column: ID
	@Parsed(index = 0)
	private String id;

	// TODO izgi Product(id=978-963-9425-95-8,

	@Schema(name = "Your product’s name", example = "Mens Pique Polo Shirt")
	@NotNull(message = "title is required")
	@Size(max = 150, message = "title max 150 character")
	// Column: Title
	@Parsed(index = 1, defaultNullRead = "default")
	private String title;

	@Schema(name = "Your product’s description", example = "Made from 100% organic cotton, this classic red men’s polo has a slim fit and signature logo embroidered on the left chest. Machine wash cold; imported.")
	@NotNull(message = "description is required")
	@Size(max = 5000, message = "title max 5000 character")
	// Column: Description
	@Parsed(index = 2, defaultNullRead = "default")
	private String description;

	@Schema(name = "Your product's availability", example = "in_stock")
	@NotNull(message = "availability is required")
	// Column: Availability
	@Parsed(index = 3)
	@EnumOptions(customElement = "fromDescription")
	private Availability availability;

	@Schema(name = "The condition of your product at time of sale", example = "new")
	// Column: Condition
	@Parsed(index = 4, defaultNullRead = "NEW")
	@EnumOptions(customElement = "fromValue")
	private Condition condition;

	@Schema(name = "Your products price", example = "15.00 USD")
	@NotNull(message = "price is required")
	@Parsed(index = 5)
	@Convert(conversionClass = MonetaryAmount.class)
	private MonetaryAmount price;

	@Schema(name = "Your product's sale price", example = "15.00 USD")
	@Parsed(index = 6)
	@Convert(conversionClass = MonetaryAmount.class)
	private MonetaryAmount sale_price;

	@Schema(name = "Your product’s landing page", example = "http://www.example.com/asp/sp.asp?cat=12&id=1030")
	@NotNull(message = "link is required")
	@URL(regexp = "^(http|https).*")
	@Parsed(index = 7)
	private String link;

	@Schema(name = "Your product’s brand name", example = "Google")
	@Size(max = 70, message = "brand max 70 character")
	@Parsed(index = 8)
	private String brand;

	@AssertTrue(message = "brand is required for new products")
	private boolean isBrandValid() {
		if (!Condition.NEW.equals(condition)) {
			return true;
		}
		return !StringUtils.isEmpty(brand);
	}

	@Schema(name = "The URL of your product’s main image", example = "http:// www.example.com/image1.jpg")
	@NotNull(message = "image_link is required")
	@URL(regexp = "^(http|https).*")
	@Parsed(index = 9)
	private String image_link;

	@Schema(name = "The demographic for which your product is intended", example = "infant")
	@Parsed(index = 10)
	@EnumOptions(customElement = "fromValue")
	private AgeGroup age_group;

	@AssertTrue(message = "age_group is required for Apparel & Accessories products")
	private boolean isAgeGroupValid() {
		if (StringUtils.isEmpty(google_product_category)) {
			return true;
		}
		if (!"166".equals(google_product_category) 
				&& 
				!google_product_category.contains("Apparel & Accessories")) {
			return true;
		}
		return null == age_group;
	}

	@Schema(name = "Google-defined product category for your product", example = "371")
	@Parsed(index = 11)
	private String google_product_category;

	@AssertTrue(message = "google_product_category should be in the list")
	private boolean isGoogleProductCategoryValid() {
		if (StringUtils.isEmpty(google_product_category)) {
			return true;
		}
		return GoogleProductCategoryValidator.isValid(google_product_category);
	}

}
