package hu.exercise.spring.kafka.listener;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductUpdateListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductUpdateListener.class);
	
	@Autowired
	private ProductService productService;
	
	private int count=0;

	@KafkaListener(topics = "product-UPDATE",
			 containerFactory = "pollingKafkaListenerContainerFactory")
	public void productPairListener(List<Product> productList) {
		int size = productList.size();
		LOGGER.warn("Received Product UPDATE message: " + size + " sum: " + (count+=size));
		
		productService.bulkSaveProducts(productList);
		
	}
	
//	public static void main(String[] args) {
//		System.out.println(new Date(1715095281247L));
//	}
}