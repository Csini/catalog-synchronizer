package hu.exercise.spring.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.input.tsv.TSVHandler;

@Component
public class KafkaCommandLineAppStartupRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCommandLineAppStartupRunner.class);

	@Autowired
	private TSVHandler tsvHandler;

	@Autowired
	private DBProductMessageProducer dbProductMessageProducer;

	@Autowired
	ShutdownController shutdownController;

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("args: " + args);

		// TODO
		tsvHandler.processInputFile("/input/file1.txt");
//		tsvHandler.processInputFile("/input/file2.txt");
//		tsvHandler.processInputFile("/input/file3.txt");

		dbProductMessageProducer.sendMessages();

//		shutdownController.shutdownContext();
	}
}