package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import hu.exercise.spring.kafka.input.Run;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Service
public class RunMessageProducer {

	@Autowired
	private KafkaTemplate<String, Run> runKafkaTemplate;
	
	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;
	
	@AsyncPublisher(operation = @AsyncOperation(channelName = "#{kafkaTopicConfig.runsTopicName}", description = "All the Runs started from catalog-syncronizer."))
	public void sendRunMessage(Run run) {
		runKafkaTemplate.send(kafkaTopicConfig.getRunsTopicName(), run);
	}
}
