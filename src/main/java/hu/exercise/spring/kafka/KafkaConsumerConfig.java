package hu.exercise.spring.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import hu.exercise.spring.kafka.cogroup.ProductRollup;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

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

	public ConsumerFactory<String, ProductRollup> productPairConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "product-intercept");

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		props.put(JsonDeserializer.TRUSTED_PACKAGES, "hu.exercise.spring.kafka.input");

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				new JsonDeserializer<>(ProductRollup.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ProductRollup> productPairKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ProductRollup> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(productPairConsumerFactory());
		return factory;
	}

}
