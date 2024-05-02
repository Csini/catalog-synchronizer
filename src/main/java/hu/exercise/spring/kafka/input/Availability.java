package hu.exercise.spring.kafka.input;

import java.util.Arrays;

public enum Availability {

	IN_STOCK("\"in stock\"", "in_stock"), 
	OUT_OF_STOCK("\"out of stock\"", "out_of_stock"), 
	PREORDER("\"preorder\"", "preorder"),
	BACKORDER("\"backorder\"", "backorder");

	private String description;

	private String value;

	private Availability(String description, String value) {
		this.description = description;
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}

	public static Availability fromValue(String value) {
		if (null == value) {
			throw new IllegalArgumentException("value is required");
		}
		return Arrays.stream(values()).filter(a -> value.equals(a.getValue())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid value: " + value));
	}
	
	public static Availability fromDescription(String description) {
		if (null == description) {
			throw new IllegalArgumentException("description is required");
		}
		return Arrays.stream(values()).filter(a -> description.equals(a.getDescription())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("invalid description: " + description));
	}

}
