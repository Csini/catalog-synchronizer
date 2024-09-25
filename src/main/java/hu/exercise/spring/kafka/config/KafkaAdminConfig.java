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
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.topic.DBEvent;
import hu.exercise.spring.kafka.topic.Flushed;
import hu.exercise.spring.kafka.topic.ProductErrorEvent;
import hu.exercise.spring.kafka.topic.ProductEvent;
import hu.exercise.spring.kafka.topic.ReadedFromDBEvent;
import hu.exercise.spring.kafka.topic.ValidProductEvent;

@Configuration
public class KafkaAdminConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;
	
	@Autowired
	public KafkaEnvironment environment;
	
	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configs.put(AdminClientConfig.CLIENT_ID_CONFIG, environment.getRequestid().toString());
		KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
			int partition = 1;
			short replicationFactor = 1;
			kafkaAdmin.createOrModifyTopics(
					new NewTopic(ReadedFromDBEvent.class.getName(), partition, replicationFactor), 
					new NewTopic(ValidProductEvent.class.getName(), partition, replicationFactor), 
					new NewTopic(ProductErrorEvent.class.getName(), partition, replicationFactor), 
					new NewTopic(Flushed.class.getName(), partition, replicationFactor), 
					new NewTopic(ProductEvent.class.getName(), partition, replicationFactor), 
					new NewTopic(DBEvent.class.getName(), partition, replicationFactor), 
					new NewTopic(Run.class.getName(), partition, replicationFactor)
					);
			return kafkaAdmin;
	}
}
