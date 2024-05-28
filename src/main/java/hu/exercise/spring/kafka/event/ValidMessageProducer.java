package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Service
public class ValidMessageProducer extends ProductEventMessageProducer {

	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private KafkaTemplate<String, ProductEvent> validFromTSVKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "#{kafkaTopicConfig.validProductName}", description = "All the valid Products readed by the request from the input TSV."))
	public void sendEvent(ProductEvent productEvent) {
		environment.getReport().setCountReadedFromTsvValid(environment.getReport().getCountReadedFromTsvValid() + 1);

		validFromTSVKafkaTemplate.send(kafkaTopicConfig.getValidProductName(),
				"" + productEvent.getRequestid() + "." + productEvent.getId(), productEvent);
		super.sendProductMessage(productEvent);
	}
}
