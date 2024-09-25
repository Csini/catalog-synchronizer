package hu.exercise.spring.kafka.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.cogroup.ProductPair;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.ReadedFromDBEvent;
import hu.exercise.spring.kafka.event.ValidProductEvent;
import hu.exercise.spring.kafka.input.Product;

@Configuration
public class KafkaSerdeConfig {

	@Bean
	public Serde<ProductRollup> productRollupSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductRollup.class));
	}

	@Bean
	public Serde<ProductPair> productPairSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductPair.class));
	}

	@Bean
	public Serde<ProductEvent> productEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductEvent.class));
	}
	
	@Bean
	public Serde<ValidProductEvent> validProductEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ValidProductEvent.class));
	}

	@Bean
	public Serde<ReadedFromDBEvent> readedFromDBEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ReadedFromDBEvent.class));
	}
	
	@Bean
	public Serde<Product> productSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Product.class));
	}

	@Bean
	public Serde<Flushed> flushedSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Flushed.class));
	}
}
