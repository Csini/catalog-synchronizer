package hu.exercise.spring.kafka.cogroup;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import hu.exercise.spring.kafka.event.ProductEvent;

public final class CustomSerdes {
	private CustomSerdes() {
	}

	public static Serde<ProductEvent> ProductEvent() {
		JsonSerializer<ProductEvent> serializer = new JsonSerializer<>();
		JsonDeserializer<ProductEvent> deserializer = new JsonDeserializer<>(ProductEvent.class);
		deserializer.addTrustedPackages("hu.exercise.spring.kafka.input");
		return Serdes.serdeFrom(serializer, deserializer);
	}

	public static Serde<ProductRollup> ProductRollup() {
		JsonSerializer<ProductRollup> serializer = new JsonSerializer<>();
		JsonDeserializer<ProductRollup> deserializer = new JsonDeserializer<>(ProductRollup.class);
		deserializer.addTrustedPackages("hu.exercise.spring.kafka.input");
		return Serdes.serdeFrom(serializer, deserializer);
	}
}