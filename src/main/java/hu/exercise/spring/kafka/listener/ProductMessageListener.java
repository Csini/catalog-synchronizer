package hu.exercise.spring.kafka.listener;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.ShutdownController;
import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.event.ProductEventMessageProducer;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMessageListener.class);

//	@Autowired
//	public NewTopic mergedProductEvents;

//	private CountDownLatch productPairLatch = new CountDownLatch(1);

	@Autowired
	private ProductService productService;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public NewTopic productRollup;

	@Autowired
	public ProductEventMessageProducer productEventMessageProducer;

	@Autowired
	ShutdownController shutdownController;

	@KafkaListener(topics = "#{__listener.topic}", containerFactory = "productPairKafkaListenerContainerFactory", batch = "true")
	public void productPairListener(Flushed flushed) {
		LOGGER.warn("Received flushed message: " + flushed);

		int countEvent = productEventMessageProducer.getCounter();
		int countProcessed = flushed.getCountProcessed();
		LOGGER.warn("countEvent: " + countEvent);
		BigDecimal temp = BigDecimal.valueOf(countProcessed).divide(BigDecimal.valueOf(countEvent), 2, RoundingMode.CEILING);
		long i = temp.multiply(BigDecimal.valueOf(100)).intValue();
		
		LOGGER.warn("i: " + i);

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; j++) {
			sb.append("#");
		}
		System.out.print("[" + String.format("%-100s", sb.toString()) + "] " + i + "%\r");

		if (countProcessed == countEvent) {
			shutdownController.shutdownContext();
		}

	}

	public String getTopic() {
		return productRollup.name();
	}

}