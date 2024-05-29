package hu.exercise.spring.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hu.exercise.spring.kafka.KafkaEnvironment;
import lombok.Getter;

@Configuration
@Getter
public class KafkaTopicConfig {

	@Value(value = "${topic.name.readedFromDb}")
	private String readedFromDbName;

	@Value(value = "${topic.name.validProduct}")
	private String validProductName;

	@Value(value = "${topic.name.invalidProduct}")
	private String invalidProductName;

	@Value(value = "${topic.name.flushed}")
	private String flushedName;

	@Value(value = "${topic.name.product}")
	private String productTopicName;
	
	@Value(value = "${topic.name.dbevent}")
	private String dbEventTopicName;
	
	@Value(value = "${topic.name.runs}")
	private String runsTopicName;

	@Autowired
	public KafkaEnvironment environment;

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
