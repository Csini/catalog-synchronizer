package hu.exercise.spring.kafka;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import hu.exercise.spring.kafka.output.Testsuites;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

@Controller
public class KafkaReportController {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReportController.class);

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public Marshaller jaxbMarshaller;

	public void createReport() throws IOException, JAXBException, URISyntaxException {

		writeFile(environment.getReport().createTestsuites());

	}

	private void writeFile(Testsuites testsuites) throws URISyntaxException, IOException, JAXBException {

		String reportfilename = "report-" + environment.getRequestid() + ".xml";

		File input = new File(KafkaReportController.class.getResource("/").toURI());
		File file = new File(input.getParentFile().getParentFile().getAbsolutePath() + "/src/main/resources/output/"
				+ reportfilename);
		file.getParentFile().mkdirs();
		file.createNewFile();
		LOGGER.warn("creating " + file.getAbsolutePath());

		jaxbMarshaller.marshal(testsuites, file);
	}

}
