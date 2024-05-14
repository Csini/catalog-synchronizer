package hu.exercise.spring.kafka.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductUpdateListener.class);

	@Autowired
	private ProductService productService;

	@Autowired
	public KafkaEnvironment environment;

	private int count = 0;

	@KafkaListener(topics = "#{__listener.topic}", containerFactory = "pollingUpdateKafkaListenerContainerFactory")
	public void productPairListener(List<Product> productList) {
		int size = productList.size();
		LOGGER.warn("Received Product UPDATE message: " + size + " sum: " + (count += size));

		// TODO
		productService.bulkSaveProducts(productList);

	}

	public String getTopic() {
		return environment.getRequestid().toString() + "-" + "product-" + Action.UPDATE;
	}

//	public static void main(String[] args) {
//		System.out.println(new Date(1715095281247L));
//	}
}