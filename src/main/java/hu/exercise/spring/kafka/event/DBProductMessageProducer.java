package hu.exercise.spring.kafka.event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaApplication;
import hu.exercise.spring.kafka.service.ProductService;
import jakarta.annotation.PostConstruct;

@Service
public class DBProductMessageProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBProductMessageProducer.class);

	@Autowired
	public NewTopic readedFromDb;
	
	@Autowired
	private KafkaTemplate<String, ProductEvent> readedFromDbKafkaTemplate;

	@Autowired
	private ProductService productService;

	public void sendMessages() throws IOException {
		
		backupDB();
		
		productService.getAllProducts().forEach(p -> {

			// LOGGER.info("sending product to readedFromDb: " + p);
			readedFromDbKafkaTemplate.send(readedFromDb.name(), p.getId(),new ProductEvent(Source.DB, p));
		});
	}

	private void backupDB() throws IOException {
		LOGGER.info("creating DB backup...");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		try (InputStream origin = KafkaApplication.class.getResourceAsStream("/products.db")) {
			Path destination = Paths.get("bkp/productsdb-" + LocalDateTime.now().format(formatter) + ".bak");

			Files.createDirectories(destination.getParent());
			Files.copy(origin, destination);

			LOGGER.info(destination.getFileName() + " in " + destination.getParent() + " is ready.");
		}
	}

}
