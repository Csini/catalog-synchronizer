package hu.exercise.spring.kafka.input;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.EnumOptions;
import com.univocity.parsers.annotations.Parsed;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link} https://support.google.com/merchants/answer/7052112?hl=en#zippy=%2Cother-requirements%2Cformatting-your-product-data
 * 
 * @author csini
 *
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PRODUCT")
@EqualsAndHashCode(of = "id")
public class Product implements Persistable<String> {

	@Id
	@Schema(description = "Your product’s unique identifier", example = "A2B4")
	@NotNull(message = "id is required")
	@Size(max = 50, min = 1, message = "id max 50 character")
	// Column: ID
	@Parsed(index = 0)
	private String id;

	// TODO izgi Product(id=978-963-9425-95-8,

	@Schema(description = "Your product’s name", example = "Mens Pique Polo Shirt")
	@NotNull(message = "title is required")
	@Size(max = 150, min = 1, message = "title max 150 character")
	// Column: Title
	@Parsed(index = 1, defaultNullRead = "default")
	private String title;

	@Schema(description = "Your product’s description", example = "Made from 100% organic cotton, this classic red men’s polo has a slim fit and signature logo embroidered on the left chest. Machine wash cold; imported.")
	@NotNull(message = "description is required")
	@Size(max = 5000, min = 1, message = "title max 5000 character")
	// Column: Description
	@Parsed(index = 2, defaultNullRead = "default")
	private String description;

	@Schema(description = "Your product's availability", example = "in_stock")
	@NotNull(message = "availability is required")
	// Column: Availability
	@Parsed(index = 3)
	@EnumOptions(customElement = "fromDescription")
	@Enumerated(EnumType.STRING)
	@Valid
	private Availability availability;

	@Schema(description = "The condition of your product at time of sale", example = "new")
	// Column: Condition
	@Parsed(index = 4, defaultNullRead = "NEW")
	@EnumOptions(customElement = "fromValue")
	@Enumerated(EnumType.STRING)
	@Valid
	private Condition condition;

	@Schema(description = "Your products price", example = "15.00 USD")
	@NotNull(message = "price is required")
	@Parsed(index = 5)
	@Convert(conversionClass = MonetaryAmount.class)
	@AttributeOverrides({ @AttributeOverride(name = "amount", column = @Column(name = "priceAmount")),
			@AttributeOverride(name = "currency", column = @Column(name = "priceCurrency")) })
	@Embedded
	@Valid
	private MonetaryAmount price;

	@Schema(description = "Your product's sale price", example = "15.00 USD")
	@Parsed(index = 6)
	@Convert(conversionClass = MonetaryAmount.class)
	@AttributeOverrides({ @AttributeOverride(name = "amount", column = @Column(name = "salePriceAmount")),
			@AttributeOverride(name = "currency", column = @Column(name = "salePriceCurrency")) })
	@Embedded
	@Valid
	private MonetaryAmount sale_price;

	@Schema(description = "Your product’s landing page", example = "http://www.example.com/asp/sp.asp?cat=12&id=1030")
	@NotNull(message = "link is required")
	@URL(regexp = "^(http|https).*")
	@Parsed(index = 7)
	private String link;

	@Schema(description = "Your product’s brand name", example = "Google")
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

	@Schema(description = "The URL of your product’s main image", example = "http:// www.example.com/image1.jpg")
	@NotNull(message = "image_link is required")
	@URL(regexp = "^(http|https).*")
	@Parsed(index = 9)
	private String image_link;

	@Schema(description = "The demographic for which your product is intended", example = "infant")
	@Parsed(index = 10)
	@EnumOptions(customElement = "fromValue")
	@Enumerated(EnumType.STRING)
	@Valid
	private AgeGroup age_group;

	@AssertTrue(message = "age_group is required for Apparel & Accessories products")
	private boolean isAgeGroupValid() {
		if (StringUtils.isEmpty(google_product_category)) {
			return true;
		}
		if (!"166".equals(google_product_category) && !google_product_category.contains("Apparel & Accessories")) {
			return true;
		}
		return null == age_group;
	}

	@Schema(description = "Google-defined product category for your product", example = "371")
	@Parsed(index = 11)
	private String google_product_category;

	@AssertTrue(message = "google_product_category should be in the list")
	private boolean isGoogleProductCategoryValid() {
		if (StringUtils.isEmpty(google_product_category)) {
			return true;
		}
		return GoogleProductCategoryValidator.isValid(google_product_category);
	}

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date created;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "requestid", nullable = false)
	private Run run;

	/**
	 * Fill current object fields with new object values, ignoring new NULLs. Old
	 * values are overwritten.
	 *
	 * @param newObject Same type object with new values.
	 * @see https://stackoverflow.com/a/56289053
	 */
	public void merge(Object newObject) {

		assert this.getClass().getName().equals(newObject.getClass().getName());

		for (Field field : this.getClass().getDeclaredFields()) {

			for (Field newField : newObject.getClass().getDeclaredFields()) {

				if ("change".equals(field.getName())) {
					continue;
				}

				if ("insert".equals(field.getName())) {
					continue;
				}

				if (field.getName().equals(newField.getName())) {

					try {
						Object value = field.get(this);
						Object newValue = newField.get(newObject);
//						System.out.println("changed? "+ field.getName() + " " + value +" -> " + newValue);

						if (value == null && newValue != null) {

							if (!"run".equals(field.getName()) && !"created".equals(field.getName())
									&& !"updated".equals(field.getName())) {
								this.change = true;
							}
						}

						if (value != null && !value.equals(newValue)) {

							if (!"run".equals(field.getName()) && !"created".equals(field.getName())
									&& !"updated".equals(field.getName())) {
								this.change = true;
							}
						}

						// field.set(this, newField.get(newObject) == null ? field.get(this) :
						// newField.get(newObject));
						field.set(this, newValue);

					} catch (IllegalAccessException ignore) {
						// Field update exception on final modifier and other cases.
					}
				}
			}
		}
	}

	@Transient
	private boolean change;

	@Transient
	private boolean insert;

	@JsonIgnore
	@Override
	public boolean isNew() {
		return this.insert;
	}

	public void setNew(boolean insert) {
		this.insert = insert;
	}

	@JsonIgnore
	public boolean isChange() {
		return change;
	}

	@JsonIgnore
	public boolean isInsert() {
		return insert;
	}

}