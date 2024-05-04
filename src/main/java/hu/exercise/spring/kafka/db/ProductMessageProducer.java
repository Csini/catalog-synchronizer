package hu.exercise.spring.kafka.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaApplication;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductMessageProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMessageProducer.class);

	@Autowired
	private KafkaTemplate<String, Product> readedFromDbKafkaTemplate;

	@Autowired
	private ProductService productService;

	public void sendMessages() {
		productService.getAllProducts().forEach(p -> {

			//LOGGER.info("sending product to readedFromDb: " + p);
			readedFromDbKafkaTemplate.send("readedFromDb", p);
		});
	}

}
