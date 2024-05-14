package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Run;

@Service
public class RunMessageProducer {

	@Autowired
	private KafkaTemplate<String, Run> runKafkaTemplate;
	
	public void sendRunMessage(Run run) {
		runKafkaTemplate.send("runs", run);
	}
}
