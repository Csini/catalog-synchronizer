package hu.exercise.spring.kafka.event;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Service
public class DBEventMessageProducer {

	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private KafkaTemplate<String, DBEvent> dbEventTopicKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "#{kafkaTopicConfig.dbEventTopicName}", description = "All the DB Actions done by the request."))
	public CompletableFuture<SendResult<String, DBEvent>> sendMessage(DBEvent event) {
		return dbEventTopicKafkaTemplate.send(kafkaTopicConfig.getDbEventTopicName(),
				event.getRequestid().toString() /* + "." + event.getId() */, event);
	}
}
