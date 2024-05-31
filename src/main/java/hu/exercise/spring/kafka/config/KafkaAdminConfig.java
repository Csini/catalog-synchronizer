package hu.exercise.spring.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import hu.exercise.spring.kafka.KafkaEnvironment;

@Configuration
public class KafkaAdminConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;
	
	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;
	
	@Autowired
	public KafkaEnvironment environment;
	
	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configs.put(AdminClientConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
		kafkaAdmin.createOrModifyTopics(
				kafkaTopicConfig.readedFromDb(), 
				kafkaTopicConfig.validProduct(), 
				kafkaTopicConfig.invalidProduct(), 
				kafkaTopicConfig.flushed(),
				kafkaTopicConfig.productTopic(), 
				kafkaTopicConfig.dbEventTopic(),
				kafkaTopicConfig.runs());
		return kafkaAdmin;
	}
}
