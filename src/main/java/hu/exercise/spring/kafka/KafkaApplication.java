package hu.exercise.spring.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaApplication.class);

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(KafkaApplication.class, args);
		} catch (Throwable e) {
			LOGGER.error("KafkaApplication", e);
			throw e;
		}
	}

}
