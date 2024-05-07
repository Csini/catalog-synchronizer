package hu.exercise.spring.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.cogroup.ProductRollup;

@Service
public class ProductMessageListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMessageListener.class);
	
//	@Autowired
//	public NewTopic mergedProductEvents;

//	private CountDownLatch productPairLatch = new CountDownLatch(1);

	@KafkaListener(topics = "${productRollup.topic.name}",
			 containerFactory = "productPairKafkaListenerContainerFactory")
	public void productPairListener(ProductRollup productPair) {
		LOGGER.info("Received ProductPair message: " + productPair.getId() + " " + productPair.getPair().getAction());
//		this.productPairLatch.countDown();
	}

}