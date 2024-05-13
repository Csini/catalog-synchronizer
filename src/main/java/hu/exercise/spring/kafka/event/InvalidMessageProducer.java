package hu.exercise.spring.kafka.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvalidMessageProducer {
	@Autowired
	public NewTopic invalidProduct;

	@Autowired
	private KafkaTemplate<String, ProductErrorEvent> invalidFromTSVKafkaTemplate;

	public void sendEvent(ProductErrorEvent productErrorEvent) {

		invalidFromTSVKafkaTemplate.send(invalidProduct.name(), "" + productErrorEvent.getRequestid(),
				productErrorEvent);
	}
}
