package hu.exercise.spring.kafka.event;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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

	protected CompletableFuture<SendResult<String,ProductEvent>> sendProductMessage(ProductEvent event) {

//		environment.getReport().setSumEvent(environment.getReport().getSumEvent() + 1);
		return productTopicKafkaTemplate.send(productTopic.name(), event.getRequestid().toString() /* + "." + event.getId() */,
				event);
	}

}
