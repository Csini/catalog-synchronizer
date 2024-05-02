package hu.exercise.spring.kafka.input;

import java.util.Arrays;

public enum AgeGroup {

	NEWBORN("Newborn", "0-3 months old", "newborn"), 
	INFANT("Infant", "3-12 months old", "infant"),
	TODDLER("Toddler", "1-5 years old", "toddler"), 
	KIDS("Kids", "5-13 years old", "kids"),
	ADULT("Adult", "Teens or older", "adult");

	private String shortname;

	private String description;

	private String value;

	private AgeGroup(String shortname, String description, String value) {
		this.shortname = shortname;
		this.description = description;
		this.value = value;
	}

	public String getShortname() {
		return shortname;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}

	public static AgeGroup fromValue(String value) {
		if (null == value) {
			throw new IllegalArgumentException("value is required");
		}
		return Arrays.stream(values()).filter(a -> value.equals(a.getValue())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid value: " + value));
	}

	public static AgeGroup fromDescription(String description) {
		if (null == description) {
			throw new IllegalArgumentException("description is required");
		}
		return Arrays.stream(values()).filter(a -> description.equals(a.getDescription())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid description: " + description));
	}
}