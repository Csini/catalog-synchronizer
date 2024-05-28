package hu.exercise.spring.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import lombok.Getter;

@Configuration
@Getter
public class KafkaTopicConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${readedFromDb.topic.name}")
	private String readedFromDbName;

	@Value(value = "${validProduct.topic.name}")
	private String validProductName;

	@Value(value = "${invalidProduct.topic.name}")
	private String invalidProductName;

	@Value(value = "${flushed.topic.name}")
	private String flushedName;

	@Value(value = "${product.topic.name}")
	private String productTopicName;
	
	@Value(value = "${dbevent.topic.name}")
	private String dbEventTopicName;
	
	@Value(value = "${runs.topic.name}")
	private String runsTopicName;

	@Autowired
	public KafkaEnvironment environment;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configs.put(AdminClientConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
		kafkaAdmin.createOrModifyTopics(readedFromDb(), validProduct(), invalidProduct(), flushed(),
				productTopic(), dbEventTopic(),runs());
		return kafkaAdmin;
	}

	@Bean
	public NewTopic readedFromDb() {
		return new NewTopic(readedFromDbName, 1, (short) 1);
	}

	@Bean
	public NewTopic validProduct() {
		return new NewTopic(validProductName, 1, (short) 1);
	}

	@Bean
	public NewTopic invalidProduct() {
		return new NewTopic(invalidProductName, 1, (short) 1);
	}

	@Bean
	public NewTopic flushed() {
		return new NewTopic(flushedName, 1, (short) 1);
	}

	@Bean
	public NewTopic productTopic() {
		return new NewTopic(productTopicName, 1, (short) 1);
	}
	
	@Bean
	public NewTopic dbEventTopic() {
		return new NewTopic(dbEventTopicName, 1, (short) 1);
	}

	@Bean
	public NewTopic runs() {
		return new NewTopic(runsTopicName, 1, (short) 1);
	}
	
}
