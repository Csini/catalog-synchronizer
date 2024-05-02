package hu.exercise.spring.kafka.input;

import java.util.Arrays;

public enum Condition {

	NEW("New", "Brand new, original, unopened packaging", "new"), 
	REFURBISHED("Refurbished", "Professionally restored to working order, comes with a warranty, may or may not have the original packaging", "refurbished"), 
	USED("User", "Previously used, original packaging opened or missing", "used");

	private String shortname;

	private String description;

	private String value;

	private Condition(String shortname, String description, String value) {
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

	public static Condition fromValue(String value) {
		if (null == value) {
			throw new IllegalArgumentException("value is required");
		}
		return Arrays.stream(values()).filter(a -> value.equals(a.getValue())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid value: " + value));
	}

	public static Condition fromDescription(String description) {
		if (null == description) {
			throw new IllegalArgumentException("description is required");
		}
		return Arrays.stream(values()).filter(a -> description.equals(a.getDescription())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid description: " + description));
	}

}
