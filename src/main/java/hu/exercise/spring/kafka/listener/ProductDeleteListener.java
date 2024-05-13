package hu.exercise.spring.kafka.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductDeleteListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDeleteListener.class);
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	public KafkaEnvironment environment;
	
	private int count=0;

	@KafkaListener(topics = "#{__listener.topic}",
			 containerFactory = "pollingDeleteKafkaListenerContainerFactory")
	public void productPairListener(List<Product> productList) {
		int size = productList.size();
		LOGGER.warn("Received Product DELETE message: " + size + " sum: " + (count+=size));
		
		productService.bulkDeleteProducts(productList);
		
	}
	
	public String getTopic() {
		return environment.getRequestid().toString() + "-" + "product-" + Action.DELETE;
	}

}