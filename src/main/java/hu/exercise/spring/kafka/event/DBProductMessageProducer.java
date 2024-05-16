package hu.exercise.spring.kafka.event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.exercise.spring.kafka.KafkaApplication;
import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class DBProductMessageProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBProductMessageProducer.class);

	@Autowired
	public NewTopic readedFromDb;

	@Autowired
	private KafkaTemplate<String, ProductEvent> readedFromDbKafkaTemplate;

	@Autowired
	private ProductService productService;

	@Autowired
	public KafkaEnvironment environment;

//	@PersistenceContext
//	public EntityManager entityManager;

	@Autowired
	private ProductEventMessageProducer productEventMessageProducer;
	
	private int counter;
	
	@Autowired
	public Report report;

//	@Transactional(readOnly = true)
//	@Transactional
	public void sendMessages() throws IOException {
		LOGGER.info("sendMessages");
		try {
			backupDB();

			productService.getAllProducts(String.valueOf(environment.getRequestid()))
					// .peek(entityManager::detach)
					.forEach(p -> {

						counter++;
						report.setCountReadedFromDB(report.getCountReadedFromDB()+1);
						
//				if (!String.valueOf(environment.getRequestid()).equals(p.getRun().getRequestid())) {
						LOGGER.info("sending product to readedFromDb: " + p);
						ProductEvent event = new ProductEvent(p.getId(), environment.getRequestid(), Source.DB, p);
						readedFromDbKafkaTemplate.send(readedFromDb.name(),
								environment.getRequestid() + "." + p.getId(), event);

						productEventMessageProducer.sendMessage(event);
//				}
					});
		} catch (IOException e) {
			LOGGER.error("", e);
			throw e;
		}
		report.setCountReadedFromDB(counter);
		LOGGER.warn("sending events to readedFromDb: " + counter);
	}

	private void backupDB() throws IOException {
		LOGGER.info("creating DB backup...");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		try (InputStream origin = KafkaApplication.class.getResourceAsStream("/products.db")) {
			Path destination = Paths.get("bkp/productsdb-" + environment.getRequestid() + ".bak");

			Files.createDirectories(destination.getParent());
			Files.copy(origin, destination);

			LOGGER.info(destination.getFileName() + " in " + destination.getParent() + " is ready.");
		}

		// INSERT INTO new_db.table_name SELECT * FROM old_db.table_name;
	}
	
	public int getCounter() {
		return counter;
	}

}
