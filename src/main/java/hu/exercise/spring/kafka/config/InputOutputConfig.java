package hu.exercise.spring.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class InputOutputConfig {

	@Value(value = "${path.input}")
	private String inputPath;

	@Value(value = "${path.output}")
	private String outputPath;
}
