package hu.exercise.spring.kafka.event;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;

@Service
public class DBEventMessageProducer {

	@Autowired
	public NewTopic dbEventTopic;

	@Autowired
	private KafkaTemplate<String, DBEvent> dbEventTopicKafkaTemplate;

	@Autowired
	public KafkaEnvironment environment;

	public CompletableFuture<SendResult<String, DBEvent>> sendMessage(DBEvent event) {
		return dbEventTopicKafkaTemplate.send(dbEventTopic.name(), event.getRequestid().toString() /* + "." + event.getId() */,
				event);
	}
}
