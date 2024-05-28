package hu.exercise.spring.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.RunMessageProducer;
import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.service.RunService;
import hu.exercise.spring.kafka.tsv.TSVHandler;

@SpringBootApplication
public class KafkaApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaApplication.class);

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(KafkaApplication.class, args);

//		KafkaStreams streams = null;
//		try (ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);) {
//
//			LOGGER.info("args: " + args);
//
//			TSVHandler tsvHandler = context.getBean(TSVHandler.class);
//
//			DBProductMessageProducer productMessageProducer = context.getBean(DBProductMessageProducer.class);
//
//			KafkaEnvironment environment= context.getBean(KafkaEnvironment.class);
//
//			RunService runService = context.getBean(RunService.class);
//
//			RunMessageProducer runMessageProducer = context.getBean(RunMessageProducer.class);
//			
//			// save metadata
//
//			Run run = environment.getRun();
//			// TODO args[0]
//			run.setFilenane("/input/file2.txt");
//
//			runService.saveRun(run);
//			runMessageProducer.sendRunMessage(run);
//
//			productMessageProducer.sendMessages();
//
//			// TODO
//			tsvHandler.processInputFile();
//			
////			LOGGER.info("creating DB backup...");
////
////			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
////
////			try (InputStream origin = KafkaApplication.class.getResourceAsStream("/products.db")) {
////				Path destination = Paths.get("bkp/productsdb-" + LocalDateTime.now().format(formatter) + ".bak");
////
////				Files.createDirectories(destination.getParent());
////				Files.copy(origin, destination);
////
////				LOGGER.info(destination.getFileName() + " in " + destination.getParent() + " is ready.");
////			}
//
////			CogroupingStreams cogrouping = context.getBean(CogroupingStreams.class);
////			streams = cogrouping.getStreams(loadEnvProperties());
////			streams.start();
//			
////			DBProductMessageProducer productMessageProducer = context.getBean(DBProductMessageProducer.class);
////			productMessageProducer.sendMessages();
//
////			TSVHandler tsvHandler = context.getBean(TSVHandler.class);
////
////			// TODO
//////			tsvHandler.processInputFile("/input/file1.txt");
////			tsvHandler.processInputFile("/input/file2.txt");
//////			tsvHandler.processInputFile("/input/file3.txt");
//
////		} finally {
////        context.close();
////			if (streams != null) {
//////				streams.close(Duration.of(30, ChronoUnit.SECONDS));
////				streams.close();
////			}
//		}

		} catch (Throwable e) {
			LOGGER.error("KafkaApplication", e);
			throw e;
		}
	}

//	public static Properties loadEnvProperties() throws IOException {
//		final Properties allProps = new Properties();
//		try (InputStream input = KafkaApplication.class.getResourceAsStream("/application.properties")) {
//			allProps.load(input);
//		}
//		
//		allProps.setProperty("application.id", "product-input");
//		allProps.setProperty("bootstrap.servers", allProps.getProperty("spring.kafka.bootstrap-servers"));
//
//		return allProps;
//	}

}
