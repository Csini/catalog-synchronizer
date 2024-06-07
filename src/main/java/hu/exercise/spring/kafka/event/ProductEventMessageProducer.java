package hu.exercise.spring.kafka.event;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Service
public class ProductEventMessageProducer {
	
	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private KafkaTemplate<String, ProductEvent> productTopicKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "#{kafkaTopicConfig.productTopicName}", description = "All the Products readed by the request."))
	protected CompletableFuture<SendResult<String,ProductEvent>> sendProductMessage(ProductEvent event) {

		return productTopicKafkaTemplate.send(kafkaTopicConfig.getProductTopicName(), event.getRequestid().toString() /* + "." + event.getId() */,
				event);
	}

}
