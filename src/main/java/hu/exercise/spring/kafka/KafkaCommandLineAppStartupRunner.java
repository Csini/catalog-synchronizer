package hu.exercise.spring.kafka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import hu.exercise.spring.kafka.config.DbConfig;
import hu.exercise.spring.kafka.config.KafkaStreamsConfig;
import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.RunMessageProducer;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.service.ProductService;
import hu.exercise.spring.kafka.service.RunService;
import hu.exercise.spring.kafka.tsv.InvalidExamplesHandler;
import hu.exercise.spring.kafka.tsv.TSVHandler;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import jakarta.annotation.PreDestroy;

@Component
public class KafkaCommandLineAppStartupRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCommandLineAppStartupRunner.class);

	@Autowired
	private TSVHandler tsvHandler;

	@Autowired
	private DBProductMessageProducer dbProductMessageProducer;

	@Autowired
	ShutdownController shutdownController;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public RunService runService;

	@Autowired
	private RunMessageProducer runMessageProducer;

	@Autowired
	StreamsBuilderFactoryBean factory;

	@Autowired
	KafkaStreamsConfig streamsConfig;

	@Autowired
	KafkaReportController reportController;

	@Autowired
	InvalidExamplesHandler invalidExamplesHandler;

	@Autowired
	MetricRegistry metrics;

	@Autowired
	private ProductService productService;

	@Autowired
	DbConfig dbConfig;

	Timer.Context contextAllRun;

	private boolean generatingSpringwolfOnly;

	@Override
	public void run(String... args) {
		Timer timer = metrics.timer("contextAllRun");
		contextAllRun = timer.time();

		List<String> argsList = Arrays.asList(args);
		LOGGER.warn("args: " + argsList);

		if (argsList.contains("generating-springwolf-only")) {
			LOGGER.warn("generating-springwolf-only");
			generatingSpringwolfOnly = true;
			return;
		}
		try {

			if (argsList.isEmpty()) {
				IllegalArgumentException e = new IllegalArgumentException(
						"Please run this application with an input filename as the first argument. For example like this: mvn spring-boot:run -Dspring-boot.run.arguments=\"file1.txt\"");
				LOGGER.error("", e);
				shutdownController.shutdownContextWithError(3, e);
				return;
			}

			backupDB();

			// save metadata
			Run run = environment.getRun();
			// args[0]
			run.setFilename(argsList.get(0));

			runService.saveRun(run);
			runMessageProducer.sendRunMessage(run);
//			MDC.put("requestid", environment.getRequestid().toString());

			LOGGER.warn(run.toString());

			ExecutorService service = Executors.newFixedThreadPool(3);
			Future<?> readFromDb = service.submit(() -> {
				Timer timerReadFromDB = metrics.timer("timerReadFromDB");
				try (Timer.Context contextReadFromDB = timerReadFromDB.time();) {

					Stream<Product> allProducts = productService
							.getAllProducts(String.valueOf(environment.getRequestid()));
					@NonNull
					Observable<@NonNull Product> fromStream = Observable.fromStream(allProducts);
					dbProductMessageProducer.sendMessages(fromStream);
//					productMessageProducer.sendMessages(fromStream, Source.DB);

//					fromStream.connect();
					environment.getReport().setTimeReadFromDb(contextReadFromDB.stop() / 1_000_000_000.0);
				} catch (Exception e) {
					LOGGER.error("db", e);
					throw new RuntimeException(e);
				} finally {
				}
			});
			Future<?> readFromTsv = service.submit(() -> {
				Timer timerReadFromTsv = metrics.timer("timerReadFromTsv");
				;
				try (Timer.Context contextReadFromTsv = timerReadFromTsv.time();) {
					tsvHandler.processInputFile();
					environment.getReport().setTimeReadFromTsv(contextReadFromTsv.stop() / 1_000_000_000.0);
				} catch (Exception e) {
					LOGGER.error("tsv", e);
					throw new RuntimeException(e);
				} finally {
				}
			});

			Future<?> generateInvalidExamples = service.submit(() -> {
				Timer timerGenerateInvalidExamples = metrics.timer("timerGenerateInvalidExamples");
				;
				try (Timer.Context contextGenerateInvalidExamples = timerGenerateInvalidExamples.time();) {
					environment.getReport().getInvalidExamples().addAll(invalidExamplesHandler.getInvalidExamples(10));
					environment.getReport()
							.setTimerGenerateInvalidExamples(contextGenerateInvalidExamples.stop() / 1_000_000_000.0);
				} catch (Exception e) {
					LOGGER.error("generateInvalidExamples", e);
					throw new RuntimeException(e);
				} finally {
				}
			});

			readFromDb.get();
			readFromTsv.get();
			generateInvalidExamples.get();

			service.shutdown();

			while (!service.awaitTermination(100, TimeUnit.MILLISECONDS)) {
			}

//			streamsConfig.addStateStore();
//			streamsConfig.productRollupStream();

			factory.setCleanupConfig(new CleanupConfig(true, false));
			factory.setStreamsUncaughtExceptionHandler(ex -> {
				LOGGER.error("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread", ex);
//			return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
				shutdownController.shutdownContextWithError(2, ex);
				return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
			});
			factory.start();
			environment.getReport().printProgressbar();

			LocalDateTime then = LocalDateTime.now();
			while (!factory.isRunning()) {
				if (ChronoUnit.SECONDS.between(then, LocalDateTime.now()) >= 60) {
					throw new RuntimeException("kafka-streams could not start in 60 sec.");
				}
			}

			// System.out.println(factory.getTopology().describe().toString());

		} catch (Throwable e) {
			LOGGER.error("commandline", e);
			shutdownController.shutdownContextWithError(9, e);
		}
	}

	private void backupDB() throws IOException {
		LOGGER.warn("creating DB backup...");

		String dbUrl = dbConfig.getDbfilenamewithpath();
		LOGGER.info("dbUrl: " + dbUrl);
		environment.getReport().setDbfilenamewithpath(dbUrl);

		File initialFile = new File(dbUrl);
		try (InputStream origin = new FileInputStream(initialFile)) {
			Path destination = Paths.get(dbConfig.getBkpPath() + "/"
					+ FilenameUtils.removeExtension(initialFile.getName()) + "-" + environment.getRequestid() + ".bak");

			Files.createDirectories(destination.getParent());
			Files.copy(origin, destination);

			LOGGER.warn(destination.getParent() + "/" + destination.getFileName() + " is ready.");
		}

	}

	@PreDestroy
	public void onExit() {
		try {
			LOGGER.warn("Exiting...");
			long elapsed = contextAllRun.stop();
			environment.getReport().setTimeAllRun(elapsed / 1_000_000_000.0);
			if (!generatingSpringwolfOnly) {
				reportController.createReport();
			}

		} catch (Throwable e) {
			LOGGER.error("onExit", e);
			System.exit(11);
		}
	}
}