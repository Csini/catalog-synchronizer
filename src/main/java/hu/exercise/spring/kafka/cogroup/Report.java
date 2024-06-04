package hu.exercise.spring.kafka.cogroup;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.output.Error;
import hu.exercise.spring.kafka.output.ObjectFactory;
import hu.exercise.spring.kafka.output.Skipped;
import hu.exercise.spring.kafka.output.Testcase;
import hu.exercise.spring.kafka.output.Testsuite;
import hu.exercise.spring.kafka.output.Testsuites;
import jakarta.validation.ConstraintViolation;
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

	private int countNoChange;

	private int sumProcessed;

	private int sumEvent;

	private long sumReaded;

	private long sumDBEvents;

	private double timeAllRun;

	private double timeReadFromDb;

	private double timeReadFromTsv;

	private double timerProcessing;

	private double timerGenerateInvalidExamples;

	private List<InvalidExample> invalidExamples = new ArrayList<>();

	private int errorCode;

	private Throwable reportedThrowable;

	private Testsuites testsuites;
	
	private String dbfilenamewithpath;

	private static final ObjectFactory OBJECTFACTORY = new ObjectFactory();

	public Report(Run run) {
		super();

		Testsuites testsuites = OBJECTFACTORY.createTestsuites();

		this.testsuites = testsuites;
		this.run = run;
	}

	public Testsuites createTestsuites() {

		{
			Testsuite testsuite = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("REQUEST: " + run.getRequestid().toString());
			testsuite.setTime(timeAllRun);
		}

		Testsuite testsuiteF = OBJECTFACTORY.createTestsuite();
		testsuites.getTestsuite().add(testsuiteF);
		testsuiteF.setName("input: " + this.run.getFilename());
		
		Testsuite testsuiteO = OBJECTFACTORY.createTestsuite();
		testsuites.getTestsuite().add(testsuiteO);
		testsuiteO.setName("output: " + dbfilenamewithpath);

		if (getReportedThrowable() != null || getErrorCode() > 0) {
			Testsuite testsuiteError = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuiteError);
			testsuiteError.setName("ERROR");

			if (getErrorCode() > 0) {
				Testcase testcaseError = OBJECTFACTORY.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				testcaseError.setName("ErrorCode: " + getErrorCode());
				Error error = OBJECTFACTORY.createError();
				// TODO
//				error.setContent();
				testcaseError.getError().add(error);
			}

			if (getReportedThrowable() != null) {
				Testcase testcaseError = OBJECTFACTORY.createTestcase();
				testsuiteError.getTestcase().add(testcaseError);
				Error error = OBJECTFACTORY.createError();
				testcaseError.setName(getReportedThrowable().getClass().getName());

				error.setContent(ExceptionUtils.getStackTrace(getReportedThrowable()));
				error.setMessage(getReportedThrowable().getMessage());
				testcaseError.getError().add(error);
			}
		}

		{
			Testsuite testsuite = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Readed Valid Products:" + getSumReaded());
//			testsuite.setHostname(environment.getFilename());
			testsuite.setTime(Math.max(timeReadFromDb, timeReadFromTsv));

			{
				Testcase testcaseDb = OBJECTFACTORY.createTestcase();
				testsuite.getTestcase().add(testcaseDb);
				testcaseDb.setName("DB:" + getCountReadedFromDB());
				testcaseDb.setTime(timeReadFromDb);
			}

			{
				Testsuite tessuiteTSV = OBJECTFACTORY.createTestsuite();
				testsuite.getTestsuite().add(tessuiteTSV);
				tessuiteTSV.setName("TSV:" + getCountReadedFromTsv());
				tessuiteTSV.setTime(timeReadFromTsv);
				{
					Testcase testcaseValid = OBJECTFACTORY.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseValid);
					testcaseValid.setName("Valid:" + getCountReadedFromTsvValid());
				}

				{
					Testcase testcaseInvalid = OBJECTFACTORY.createTestcase();
					tessuiteTSV.getTestcase().add(testcaseInvalid);
					testcaseInvalid.setName("Invalid:" + getCountReadedFromTsvInvalid());
				}
			}

		}

		{
			Testsuite testsuite = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Processed Products: " + getSumProcessed());

		}
		{
			Testsuite testsuite = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Processed Events: " + getSumDBEvents());
			testsuite.setTime(timerProcessing);
			{
				Testcase testcase = OBJECTFACTORY.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("INSERT: " + getCountInsert());
			}

			{
				Testcase testcase = OBJECTFACTORY.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("UPDATE: " + getCountUpdate());
			}

			{
				Testcase testcase = OBJECTFACTORY.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("DELETE: " + getCountDelete());
			}

			{
				Testcase testcase = OBJECTFACTORY.createTestcase();
				testsuite.getTestcase().add(testcase);
				testcase.setName("NOCHANGE: " + getCountNoChange());
			}

		}

		{
			Testsuite testsuite = OBJECTFACTORY.createTestsuite();
			testsuites.getTestsuite().add(testsuite);
			testsuite.setName("Invalid EXAMPLES:");
			testsuite.setTime(timerGenerateInvalidExamples);

			invalidExamples.forEach(example -> {
				Testcase testcase = OBJECTFACTORY.createTestcase();
				testcase.setName(example.getTsvContent());
				Skipped skipped = OBJECTFACTORY.createSkipped();
				testcase.setSkipped(skipped);
				Set<ConstraintViolation<Product>> violations = example.getViolations();

				skipped.setMessage(violations.stream()
						.map(v -> v.getPropertyPath() + ": " + v.getInvalidValue() + " - " + v.getMessage())
						.collect(Collectors.joining("\n")));

				testsuite.getTestcase().add(testcase);
			});

		}

		return testsuites;
	}

	public long printProgressbar() {
		// int sumEvent = productEventMessageProducer.getCounter();
		int sumEvent = getSumEvent();
		int sumProcessed = getSumProcessed();
		LOGGER.info("sumEvent    : " + sumEvent);
		LOGGER.info("sumProcessed: " + sumProcessed);

		BigDecimal temp = BigDecimal.ZERO;

		if (sumEvent > 0) {
			temp = BigDecimal.valueOf(sumProcessed).divide(BigDecimal.valueOf(sumEvent), 2, RoundingMode.CEILING);
		}
		long i = temp.multiply(BigDecimal.valueOf(100)).intValue();

		LOGGER.info("i: " + i);

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; j++) {
			sb.append("#");
		}
		System.out.print(
				"[" + String.format("%-100s", sb.toString()) + "] " + String.format("%3s", i) + "% (" + run.getRequestid() + ")" + "\r");
		return i;
	}

	public long getSumToBeProcessed() {
		return getSumReaded() - getCountReadedFromTsvInvalid();
	}

}
