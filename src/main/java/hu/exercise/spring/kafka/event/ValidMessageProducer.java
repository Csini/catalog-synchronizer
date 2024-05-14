package hu.exercise.spring.kafka.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ValidMessageProducer {

	@Autowired
	public NewTopic validProduct;

	@Autowired
	public NewTopic invalidProduct;

//	@Autowired
//	private ProductRepository repository;

	@Autowired
	private KafkaTemplate<String, ProductEvent> validFromTSVKafkaTemplate;

	public void sendEvent(ProductEvent productEvent) {
		//TODO uuid toString
		validFromTSVKafkaTemplate.send(validProduct.name(), 
				productEvent.getRequestid().toString()+"."+ productEvent.getId()
				, productEvent);
	}
}
