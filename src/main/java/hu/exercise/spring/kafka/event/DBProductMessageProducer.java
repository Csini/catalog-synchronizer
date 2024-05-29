package hu.exercise.spring.kafka.event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaApplication;
import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import hu.exercise.spring.kafka.service.ProductService;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

@Service
public class DBProductMessageProducer extends ProductEventMessageProducer{

	private static final Logger LOGGER = LoggerFactory.getLogger(DBProductMessageProducer.class);

	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	private KafkaTemplate<String, ProductEvent> readedFromDbKafkaTemplate;

	@Autowired
	private ProductService productService;

	@Autowired
	public KafkaEnvironment environment;

	public void sendMessages() throws IOException {
		LOGGER.info("sendMessages");
		Report report = environment.getReport();
		try {
			backupDB();

			
			productService.getAllProducts(String.valueOf(environment.getRequestid()))
					.forEach(p -> {

						report.setCountReadedFromDB(report.getCountReadedFromDB() + 1);
						report.setSumReaded(report.getSumReaded() + 1);

						LOGGER.info("sending product to readedFromDb: " + p);
						ProductEvent event = new ProductEvent(p.getId(), environment.getRequestid(), Source.DB, p);
						sendEvent(event);

//						try {
							CompletableFuture<SendResult<String, ProductEvent>> sendProductMessage = super.sendProductMessage(event);
//							SendResult<String, ProductEvent> sendResult = sendProductMessage.get(10, TimeUnit.SECONDS);
//					        handleSuccess(data);
//					    }
//					    catch (ExecutionException e) {
//					        handleFailure(data, record, e.getCause());
//					    }
//					    catch (TimeoutException | InterruptedException e) {
//					        handleFailure(data, record, e);
//					    }
//						
//						sendProductMessage.whenComplete((result, ex) -> {
//					        if (ex == null) {
//					            handleSuccess(data);
//					        }
//					        else {
//					            handleFailure(data, record, ex);
//					        }
//					    });
					});
		} catch (IOException e) {
			LOGGER.error("", e);
			throw e;
		} finally {
			LOGGER.warn("sending events to readedFromDb: " + report.getCountReadedFromDB());
		}
	}

	@AsyncPublisher(operation = @AsyncOperation(channelName = "#{kafkaTopicConfig.readedFromDbName}", description = "All the Product readed from DB."))
	private void sendEvent(ProductEvent event) {
		readedFromDbKafkaTemplate.send(kafkaTopicConfig.getReadedFromDbName(), environment.getRequestid() + "." + event.getId(), event);
	}

	private void backupDB() throws IOException {
		LOGGER.info("creating DB backup...");

//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		try (InputStream origin = KafkaApplication.class.getResourceAsStream("/products.db")) {
			Path destination = Paths.get("bkp/productsdb-" + environment.getRequestid() + ".bak");

			Files.createDirectories(destination.getParent());
			Files.copy(origin, destination);

			LOGGER.info(destination.getFileName() + " in " + destination.getParent() + " is ready.");
		}

		// INSERT INTO new_db.table_name SELECT * FROM old_db.table_name;
	}

}
