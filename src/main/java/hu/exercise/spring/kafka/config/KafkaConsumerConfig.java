package hu.exercise.spring.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import hu.exercise.spring.kafka.KafkaCommandLineAppStartupRunner;
import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.ShutdownController;
import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.input.Product;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);
	
	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${kafka.backoff.interval}")
	private Long interval;

	@Value(value = "${kafka.backoff.max_failure}")
	private Long maxAttempts;
	
	@Autowired
	public KafkaEnvironment environment;
	
	@Autowired
	public ShutdownController shutdownController;

//	public ConsumerFactory<String, String> consumerFactory(String groupId) {
//		Map<String, Object> props = new HashMap<>();
//		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "20971520");
//		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "20971520");
//		return new DefaultKafkaConsumerFactory<>(props);
//	}
//
//	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(String groupId) {
//		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//		factory.setConsumerFactory(consumerFactory(groupId));
//		return factory;
//	}

	public ConsumerFactory<String, Flushed> productPairConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getRequestid().toString());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // default=500

		
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(Flushed.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Flushed> productPairKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Flushed> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(productPairConsumerFactory());
//		factory.setBatchListener(true); // <<<<<<<<<<<<<<<<<<<<<<<<<
		return factory;
	}

	public ConsumerFactory<String, Product> pollingUpdateConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getRequestid().toString());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // default=500

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(Product.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Product> pollingUpdateKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Product> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(pollingUpdateConsumerFactory());
		factory.setBatchListener(true); // <<<<<<<<<<<<<<<<<<<<<<<<<

		// Other configurations
//		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
//		factory.afterPropertiesSet();

		return factory;
	}

	public ConsumerFactory<String, Product> pollingInsertConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getRequestid().toString());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // default=500

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(Product.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Product> pollingInsertKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Product> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(pollingInsertConsumerFactory());
		factory.setBatchListener(true); // <<<<<<<<<<<<<<<<<<<<<<<<<

		// Other configurations
//		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
//		factory.afterPropertiesSet();

		return factory;
	}

	public ConsumerFactory<String, Product> pollingDeleteConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getRequestid().toString());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // default=500

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(Product.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Product> pollingDeleteKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Product> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(pollingDeleteConsumerFactory());
		factory.setBatchListener(true); // <<<<<<<<<<<<<<<<<<<<<<<<<

		// Other configurations
//		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
//		factory.afterPropertiesSet();

		return factory;
	}

	@Bean
	public DefaultErrorHandler errorHandler() {
		BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
		DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {

			// TODO
			LOGGER.error(String.format("consumed record %s because this exception was thrown",
					consumerRecord.toString(), e.getClass().getName()), e);
			shutdownController.shutdownContext();
		}, fixedBackOff);
		// Commented because of the test
//		errorHandler.addRetryableExceptions(org.sqlite.SQLiteException.class, org.springframework.orm.jpa.JpaSystemException.class);
		errorHandler.addNotRetryableExceptions(NullPointerException.class);
		return errorHandler;
	}

}
