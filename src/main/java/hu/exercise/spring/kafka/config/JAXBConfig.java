package hu.exercise.spring.kafka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.output.Testsuites;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

@Configuration
public class JAXBConfig {

	@Autowired
	public KafkaEnvironment environment;

	@Bean
	public Marshaller testSuitesMarshaller() throws JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(Testsuites.class);

		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		return jaxbMarshaller;
	}

	@Bean
	public MetricRegistry metrics() {
		return new MetricRegistry();
	}
}
