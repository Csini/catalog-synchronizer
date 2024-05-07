package hu.exercise.spring.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.ProductEvent;
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
	private KafkaTemplate<String, Run> runKafkaTemplate;

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("args: " + args);

		//save metadata

		Run run = environment.getRun();
		//TODO args[0]
		run.setFilenane("/input/file1.txt");

		runService.saveRun(run);
		runKafkaTemplate.send("runs", run);
		
		dbProductMessageProducer.sendMessages();

		// TODO
		tsvHandler.processInputFile();
//		tsvHandler.processInputFile("/input/file2.txt");
//		tsvHandler.processInputFile("/input/file3.txt");


//		shutdownController.shutdownContext();
	}
}