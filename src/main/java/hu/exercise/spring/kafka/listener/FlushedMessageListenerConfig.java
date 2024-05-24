package hu.exercise.spring.kafka.listener;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlushedMessageListenerConfig {
	
	@Autowired
	public NewTopic flushed;

	public String getTopic() {
		return flushed.name();
	}
}
