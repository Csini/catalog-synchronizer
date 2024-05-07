package hu.exercise.spring.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import hu.exercise.spring.kafka.cogroup.CogroupingStreams;
import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.input.tsv.TSVHandler;

@SpringBootApplication
public class KafkaApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaApplication.class);

	public static void main(String[] args) throws Exception {
		
		SpringApplication.run(KafkaApplication.class, args);

//		KafkaStreams streams = null;
//		try (ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);) {

//			LOGGER.info("creating DB backup...");
//
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//
//			try (InputStream origin = KafkaApplication.class.getResourceAsStream("/products.db")) {
//				Path destination = Paths.get("bkp/productsdb-" + LocalDateTime.now().format(formatter) + ".bak");
//
//				Files.createDirectories(destination.getParent());
//				Files.copy(origin, destination);
//
//				LOGGER.info(destination.getFileName() + " in " + destination.getParent() + " is ready.");
//			}

//			CogroupingStreams cogrouping = context.getBean(CogroupingStreams.class);
//			streams = cogrouping.getStreams(loadEnvProperties());
//			streams.start();
			
//			DBProductMessageProducer productMessageProducer = context.getBean(DBProductMessageProducer.class);
//			productMessageProducer.sendMessages();

//			TSVHandler tsvHandler = context.getBean(TSVHandler.class);
//
//			// TODO
////			tsvHandler.processInputFile("/input/file1.txt");
//			tsvHandler.processInputFile("/input/file2.txt");
////			tsvHandler.processInputFile("/input/file3.txt");

//		} finally {
//        context.close();
//			if (streams != null) {
////				streams.close(Duration.of(30, ChronoUnit.SECONDS));
//				streams.close();
//			}
//		}
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
