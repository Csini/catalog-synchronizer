package hu.exercise.spring.kafka;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.config.KafkaStreamsConfig;
import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.RunMessageProducer;
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.input.tsv.TSVHandler;
import hu.exercise.spring.kafka.service.RunService;

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

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("args: " + args);
		
		// save metadata

		Run run = environment.getRun();
		// TODO args[0]
		run.setFilenane("/input/file2.txt");

		runService.saveRun(run);
		runMessageProducer.sendRunMessage(run);

		LOGGER.warn(run.toString());
		
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.submit(() -> {
			try {
				dbProductMessageProducer.sendMessages();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		service.submit(() -> {
			try {
				tsvHandler.processInputFile();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		service.shutdown();
		service.awaitTermination(1, TimeUnit.MINUTES);

		
		streamsConfig.productRollupStream();
		
		
		factory.setCleanupConfig(new CleanupConfig(true, true));
		factory.start();

//		KafkaStreams kafkaStreams = factory.getKafkaStreams();
////		kafkaStreams.pause();
////		kafkaStreams.cleanUp();
//		kafkaStreams.start();
		// TODO

//		tsvHandler.processInputFile("/input/file2.txt");
//		tsvHandler.processInputFile("/input/file3.txt");

//		shutdownController.shutdownContext();
	}
}