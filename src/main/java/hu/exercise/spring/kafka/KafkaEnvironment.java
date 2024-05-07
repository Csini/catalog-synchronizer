package hu.exercise.spring.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import hu.exercise.spring.kafka.input.Run;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;

@Configuration
@Getter
public class KafkaEnvironment {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEnvironment.class);

	private Run run;
	
	@PostConstruct
	public void init() {
		this.run = new Run();
		LOGGER.info("KafkaEnvironment initialized with " + this.run);
	}

	// TODO
	@NonNull
	public UUID getRequestid() {
		return UUID.fromString(this.run.getRequestid());
	}

	// TODO
	@NonNull
	public String getFilenane() {
		return this.run.getFilenane();
	}
}
 