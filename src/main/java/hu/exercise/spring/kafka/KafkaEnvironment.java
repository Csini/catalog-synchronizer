package hu.exercise.spring.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.input.Run;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;

@Configuration
@Getter
public class KafkaEnvironment {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEnvironment.class);

	private Run run;
	
	private Report report;
	
	@PostConstruct
	public void init() {
		this.run = new Run();
		LOGGER.info("KafkaEnvironment initialized with " + this.run);
		this.report = new Report(this.run);
	}

	@NonNull
	public UUID getRequestid() {
		return UUID.fromString(this.run.getRequestid());
	}

	@NonNull
	public String getFilename() {
		return this.run.getFilename();
	}
	
	public Report getReport() {
		return this.report;
	}
	
}
 