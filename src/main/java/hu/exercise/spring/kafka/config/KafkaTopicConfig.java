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

@Configuration
public class KafkaTopicConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${readedFromDb.topic.name}")
	private String readedFromDb;

	@Value(value = "${validProduct.topic.name}")
	private String validProduct;

	@Value(value = "${invalidProduct.topic.name}")
	private String invalidProduct;

	@Value(value = "${productRollup.topic.name}")
	private String productRollup;
	
	@Value(value = "${product.topic.name}")
	private String productTopic;
	
	@Autowired
	public KafkaEnvironment environment;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
		kafkaAdmin.createOrModifyTopics(readedFromDb(), validProduct(), invalidProduct(), productRollup());
		return kafkaAdmin;
	}

	@Bean
	public NewTopic readedFromDb() {
		return new NewTopic(readedFromDb, 1, (short) 1);
	}

	@Bean
	public NewTopic validProduct() {
		return new NewTopic(validProduct, 1, (short) 1);
	}

	@Bean
	public NewTopic invalidProduct() {
		return new NewTopic(invalidProduct, 1, (short) 1);
	}

	@Bean
	public NewTopic productRollup() {
		return new NewTopic(productRollup, 1, (short) 1);
	}
	
	@Bean
	public NewTopic productTopic() {
		return new NewTopic(environment.getRequestid() + "-" + productTopic, 1, (short) 1);
	}
	
	@Bean
	public NewTopic runs() {
		return new NewTopic("runs", 1, (short) 1);
	}
	
	@Bean
	public NewTopic productUpdate() {
		return new NewTopic(environment.getRequestid() + "-" + "product-" + Action.UPDATE, 1, (short) 1);
	}
	
	@Bean
	public NewTopic productInsert() {
		return new NewTopic(environment.getRequestid() + "-" + "product-" + Action.INSERT, 1, (short) 1);
	}
	
	@Bean
	public NewTopic productDelete() {
		return new NewTopic(environment.getRequestid() + "-" + "product-" + Action.DELETE, 1, (short) 1);
	}

}
