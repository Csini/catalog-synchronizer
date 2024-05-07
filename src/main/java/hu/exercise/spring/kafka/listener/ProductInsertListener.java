package hu.exercise.spring.kafka.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductInsertListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInsertListener.class);
	
	@Autowired
	private ProductService productService;
	
	private int count=0;

	@KafkaListener(topics = "product-INSERT",
			 containerFactory = "pollingKafkaListenerContainerFactory")
	public void productPairListener(List<Product> productList) {
		int size = productList.size();
		LOGGER.warn("Received Product INSERT message: " + size + " sum: " + (count+=size));
		
		productService.bulkSaveProducts(productList);
		
	}

}