package hu.exercise.spring.kafka.init;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTopicsSpringApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateTopicsSpringApplication.class);

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();

		properties.load(
				CreateTopicsSpringApplication.class.getClassLoader().getResourceAsStream("application.properties"));

		KafkaTopicApplication kafkaTopicApplication = new KafkaTopicApplication(properties);

		kafkaTopicApplication.listTopics().forEach(topic -> {
			LOGGER.info("Topic: " + topic.name());
		});
		;

		// TODO create topics
	}
}
