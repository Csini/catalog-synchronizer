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
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.ShutdownController;
import hu.exercise.spring.kafka.cogroup.Flushed;

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

	public ConsumerFactory<String, Flushed> flushedConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getRequestid().toString());
//		props.put(ConsumerConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); // default=500

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(Flushed.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Flushed> flushedKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Flushed> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(flushedConsumerFactory());
		factory.setCommonErrorHandler(errorHandler());
//		factory.setBatchListener(true); // <<<<<<<<<<<<<<<<<<<<<<<<<
		return factory;
	}

	@Bean
	public DefaultErrorHandler errorHandler() {
		BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
		DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {

			LOGGER.error(String.format("consumed record %s because this exception was thrown",
					consumerRecord.toString(), e.getClass().getName()), e);
			shutdownController.shutdownContextWithError(9, e);
		}, fixedBackOff);
//		errorHandler.addRetryableExceptions(org.sqlite.SQLiteException.class, org.springframework.orm.jpa.JpaSystemException.class);
		errorHandler.addNotRetryableExceptions(NullPointerException.class);
		return errorHandler;
	}

}
