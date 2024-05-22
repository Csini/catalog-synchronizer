package hu.exercise.spring.kafka.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;

@Service
public class InvalidMessageProducer {
	@Autowired
	public NewTopic invalidProduct;

	@Autowired
	private KafkaTemplate<String, ProductErrorEvent> invalidFromTSVKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	public void sendEvent(ProductErrorEvent productErrorEvent) {

		environment.getReport().setCountReadedFromTsvInvalid(environment.getReport().getCountReadedFromTsvInvalid()+1);
		
		invalidFromTSVKafkaTemplate.send(invalidProduct.name(), "" + productErrorEvent.getRequestid(),
				productErrorEvent);
	}
}
