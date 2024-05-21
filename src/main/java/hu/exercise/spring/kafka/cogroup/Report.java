package hu.exercise.spring.kafka.cogroup;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.output.Error;
import hu.exercise.spring.kafka.output.ObjectFactory;
import hu.exercise.spring.kafka.output.Testcase;
import hu.exercise.spring.kafka.output.Testsuite;
import hu.exercise.spring.kafka.output.Testsuites;
import lombok.Data;

@Data
//@NoArgsConstructor
public class Report {

	private static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

	private Run run;

	private int countInsert;

	private int countUpdate;

	private int countDelete;

	private int countError;

	private int countReadedFromDB;

	private long countReadedFromTsv;
	private int countReadedFromTsvValid;
	private int countReadedFromTsvInvalid;

	private int sumProcessed;

	private int sumEvent;

	private long sumReaded;

	private long sumDBEvents;

	private int errorCode;

	private Throwable reportedThrowable;

	private Testsuites testsuites;

	public Report(Run run) {
		super();

		ObjectFactory objectFactory = new ObjectFactory();

		Testsuites testsuites = objectFactory.createTestsuites();

		Testsuite testsuite = objectFactory.createTestsuite();
		testsuites.getTestsuite().add(testsuite);
		testsuite.setName("REQUEST: " + run.getRequestid().toString());
		this.testsuites = testsuites;
		this.run = run;
	}

	public Testsuites createTestsuites() {

		ObjectFactory objectFactory = new ObjectFactory();

		Testsuite testsuiteF = objectFactory.createTestsuite();
		testsuites.getTestsuite().add(testsuiteF);
		testsuiteF.setName("input: " + this.run.getFilename());

		if (getReportedThrowable() != null || getErrorCode() > 0) {
			Testsuite testsuiteError = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuiteError);
			testsuiteError.setName("ERROR");

			if (getErrorCode() > 0) {
				Testcase testcaseError = objectFactory.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				testcaseError.setName("ErrorCode: " + getErrorCode());
				Error error = objectFactory.createError();
				// TODO
//				error.setContent();
				testcaseError.getError().add(error);
			}

			if (getReportedThrowable() != null) {
				Testcase testcaseError = objectFactory.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				Error error = objectFactory.createError();
				testcaseError.setName(getReportedThrowable().getClass().getName());

				error.setContent(ExceptionUtils.getStackTrace(getReportedThrowable()));
				error.setMessage(getReportedThrowable().getMessage());
				testcaseError.getError().add(error);
			}
		}

		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Readed Valid Products:" + getSumReaded());
//			testsuite.setHostname(environment.getFilename());

			{
				Testcase testcaseDb = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcaseDb);
				testcaseDb.setName("DB:" + getCountReadedFromDB());
			}

			{
				Testsuite tessuiteTSV = objectFactory.createTestsuite();
				testsuite.getTestsuite().add(tessuiteTSV);
				tessuiteTSV.setName("TSV:" + getCountReadedFromTsv());

				{
					Testcase testcaseValid = objectFactory.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseValid);
					testcaseValid.setName("Valid:" + getCountReadedFromTsvValid());
				}

				{
					Testcase testcaseInvalid = objectFactory.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseInvalid);
					testcaseInvalid.setName("Invalid:" + getCountReadedFromTsvInvalid());
				}
			}

		}

		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Processed Products: " + getSumProcessed());

		}
		{
			Testsuite testsuite = objectFactory.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Processed Events: " + getSumDBEvents());
			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("INSERT: " + getCountInsert());
			}

			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("UPDATE: " + getCountUpdate());
			}

			{
				Testcase testcase = objectFactory.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("DELETE: " + getCountDelete());
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

		return testsuites;
	}

	public long printProgressbar() {
		// int sumEvent = productEventMessageProducer.getCounter();
		int sumEvent = getSumEvent();
		int sumProcessed = getSumProcessed();
		LOGGER.warn("sumEvent    : " + sumEvent);
		LOGGER.warn("sumProcessed: " + sumProcessed);

		BigDecimal temp = BigDecimal.valueOf(sumProcessed).divide(BigDecimal.valueOf(sumEvent), 2,
				RoundingMode.CEILING);
		long i = temp.multiply(BigDecimal.valueOf(100)).intValue();

		LOGGER.warn("i: " + i);

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; j++) {
			sb.append("#");
		}
		System.out.print(
				"[" + String.format("%-100s", sb.toString()) + "] " + i + "% (" + run.getRequestid() + ")" + "\r");
		return i;
	}

}
