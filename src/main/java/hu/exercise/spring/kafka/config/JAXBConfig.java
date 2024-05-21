package hu.exercise.spring.kafka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.KafkaReportController;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.output.ObjectFactory;
import hu.exercise.spring.kafka.output.Properties;
import hu.exercise.spring.kafka.output.Property;
import hu.exercise.spring.kafka.output.Testsuite;
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

}
