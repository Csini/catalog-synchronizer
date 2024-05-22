package hu.exercise.spring.kafka.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;

@Service
public class ValidMessageProducer extends ProductEventMessageProducer{

	@Autowired
	public NewTopic validProduct;

	@Autowired
	private KafkaTemplate<String, ProductEvent> validFromTSVKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	public void sendEvent(ProductEvent productEvent) {
		environment.getReport().setCountReadedFromTsvValid(environment.getReport().getCountReadedFromTsvValid() + 1);

		// TODO uuid toString
		validFromTSVKafkaTemplate.send(validProduct.name(),
				productEvent.getRequestid().toString() + "." + productEvent.getId(), productEvent);
		super.sendProductMessage(productEvent);
	}
}
