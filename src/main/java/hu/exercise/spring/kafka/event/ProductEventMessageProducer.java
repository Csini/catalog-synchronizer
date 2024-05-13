package hu.exercise.spring.kafka.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;

@Service
public class ProductEventMessageProducer {
	@Autowired
	public NewTopic productTopic;

	@Autowired
	private KafkaTemplate<String, ProductEvent> productTopicKafkaTemplate;
	
	@Autowired
	public KafkaEnvironment environment;

	public void sendMessage(ProductEvent event) {
		productTopicKafkaTemplate.send(environment.getRequestid() + "-" + productTopic.name(), event.getRequestid().toString() + "." + event.getId(),
				event);
	}
}
