package hu.exercise.spring.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.event.ProductErrorEvent;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.input.*;

@Configuration
public class KafkaProducerConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;
	
	@Autowired
	public KafkaEnvironment environment;

	@Bean
	public ProducerFactory<String, ProductEvent> readedFromDbProducerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, ProductEvent> readedFromDbKafkaTemplate() {
		return new KafkaTemplate<>(readedFromDbProducerFactory());
	}
	
	@Bean
	public ProducerFactory<String, ProductEvent> validFromTSVKafkaFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, ProductEvent> validFromTSVKafkaTemplate() {
		return new KafkaTemplate<>(validFromTSVKafkaFactory());
	}
	
	@Bean
	public ProducerFactory<String, ProductErrorEvent> invalidFromTSVKafkaTemplateFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, ProductErrorEvent> invalidFromTSVKafkaTemplate() {
		return new KafkaTemplate<>(invalidFromTSVKafkaTemplateFactory());
	}

	
	@Bean
	public ProducerFactory<String, Run> runKafkaTemplateFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "common");
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, Run> runKafkaTemplate() {
		return new KafkaTemplate<>(runKafkaTemplateFactory());
	}
	
	@Bean
	public ProducerFactory<String, ProductEvent> productTopicKafkaFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, ProductEvent> productTopicKafkaTemplate() {
		return new KafkaTemplate<>(productTopicKafkaFactory());
	}
}
