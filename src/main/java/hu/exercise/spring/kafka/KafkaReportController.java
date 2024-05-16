package hu.exercise.spring.kafka;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.output.Error;
import hu.exercise.spring.kafka.output.Failure;
import hu.exercise.spring.kafka.output.ObjectFactory;
import hu.exercise.spring.kafka.output.Properties;
import hu.exercise.spring.kafka.output.Property;
import hu.exercise.spring.kafka.output.Testcase;
import hu.exercise.spring.kafka.output.Testsuite;
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

	@Autowired
	public ObjectFactory objectFactory;

	@Autowired
	public Testsuites testsuites;

	@Autowired
	public Report report;

	public void createReport() throws IOException, JAXBException, URISyntaxException {

		if (report.getReportedThrowable() != null || report.getErrorCode() > 0) {
			Testsuite testsuiteError = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuiteError);
			testsuiteError.setName("ERROR");

			if (report.getErrorCode() > 0) {
				Testcase testcaseError = objectFactory.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				testcaseError.setName("ErrorCode: " + report.getErrorCode());
				Error error = objectFactory.createError();
				// TODO
//				error.setContent();
				testcaseError.getError().add(error);
			}

			if (report.getReportedThrowable() != null) {
				Testcase testcaseError = objectFactory.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				Error error = objectFactory.createError();
				testcaseError.setName(report.getReportedThrowable().getClass().getName());
				
				error.setContent(ExceptionUtils.getStackTrace(report.getReportedThrowable()));
				error.setMessage(report.getReportedThrowable().getMessage());
				testcaseError.getError().add(error);
			}
		}
		
		
		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Readed Products:" + report.getSumEvent());
//			testsuite.setHostname(environment.getFilename());

			{
				Testcase testcaseDb = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcaseDb);
				testcaseDb.setName("DB:" + report.getCountReadedFromDB());
			}

			{
				Testsuite tessuiteTSV = objectFactory.createTestsuite();
				testsuite.getTestsuite().add(tessuiteTSV);
				tessuiteTSV.setName("TSV:" + report.getCountReadedFromTsv());

				{
					Testcase testcaseValid = objectFactory.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseValid);
					testcaseValid.setName("Valid:" + report.getCountReadedFromTsvValid());
				}

				{
					Testcase testcaseInvalid = objectFactory.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseInvalid);
					testcaseInvalid.setName("Invalid:" + report.getCountReadedFromTsvInvalid());
				}
			}

		}

		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Processed Products: " + report.getSumProcessed());
			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("INSERT: " + report.getCountInsert());
			}

			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("UPDATE: " + report.getCountUpdate());
			}

			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("DELETE: " + report.getCountDelete());
			}

			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("NOCHANGE: 0");
			}

		}
		
		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Invalid EXAMPLES:");
		}

		writeFile(testsuites);

	}

//	public void createErrorReport(int errorCode, Throwable e) throws IOException, JAXBException, URISyntaxException {
//
//		{
//			Testsuite testsuite = objectFactory.createTestsuite();
//			testsuites.getTestsuite().add(testsuite);
//			testsuite.setName("ERROR");
//
//			Testcase testcase = objectFactory.createTestcase();
//			testsuite.getTestcase().add(testcase);
//			Error error = objectFactory.createError();
//			error.setContent(ExceptionUtils.getStackTrace(e));
//			error.setMessage(e.getMessage());
//			testcase.getError().add(error);
//		}
//
//		writeFile(testsuites);
//	}

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

	private void addProperty(Properties testsuiteProperties, String name, String value) {
		Property property = objectFactory.createProperty();
		testsuiteProperties.getProperty().add(property);
		property.setName(name);
		property.setValue(value);
	}
}
