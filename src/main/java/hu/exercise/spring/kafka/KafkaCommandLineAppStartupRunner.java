package hu.exercise.spring.kafka;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import hu.exercise.spring.kafka.config.KafkaStreamsConfig;
import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.RunMessageProducer;
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.input.tsv.TSVHandler;
import hu.exercise.spring.kafka.service.RunService;
import jakarta.annotation.PreDestroy;
import jakarta.xml.bind.JAXBException;

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

//	@Autowired
//	PlatformTransactionManager txManager;

	@Autowired
	KafkaReportController reportController;

	@Override
	public void run(String... args) {
		LOGGER.info("args: " + args);

		try {

			// save metadata

//		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//		// explicitly setting the transaction name is something that can be done only
//		// programmatically
//		def.setName("SomeTxName");
//		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//		TransactionStatus status = txManager.getTransaction(def);

			Run run = environment.getRun();
			// TODO args[0]
			run.setFilename("file1.txt");

			runService.saveRun(run);
			runMessageProducer.sendRunMessage(run);
//			MDC.put("requestid", environment.getRequestid().toString());

			LOGGER.warn(run.toString());

			ExecutorService service = Executors.newFixedThreadPool(2);
			Future<?> readFromDb = service.submit(() -> {
				try {
					dbProductMessageProducer.sendMessages();
				} catch (Exception e) {
					LOGGER.error("db", e);
					throw new RuntimeException(e);
				}
			});
			Future<?> readFromTsv = service.submit(() -> {
				try {
					tsvHandler.processInputFile();
				} catch (Exception e) {
					LOGGER.error("tsv", e);
					throw new RuntimeException(e);
				}
			});

			readFromDb.get();
			readFromTsv.get();

			service.shutdown();

			while (!service.awaitTermination(100, TimeUnit.MILLISECONDS)) {
			}

			streamsConfig.productRollupStream();

			factory.setCleanupConfig(new CleanupConfig(true, false));
			factory.setStreamsUncaughtExceptionHandler(ex -> {
				LOGGER.error("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread", ex);
//			return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
				shutdownController.shutdownContextWithError(2, ex);
				return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
			});
			factory.start();

		} catch (Throwable e) {
			LOGGER.error("commandline", e);
			shutdownController.shutdownContextWithError(9, e);
		}
	}

	@PreDestroy
	public void onExit() throws IOException, JAXBException, URISyntaxException {
		LOGGER.info("###STOP FROM THE LIFECYCLE###");
		reportController.createReport();
	}
}