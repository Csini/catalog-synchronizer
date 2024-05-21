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
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.event.DBProductMessageProducer;
import hu.exercise.spring.kafka.event.ProductEventMessageProducer;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class FlushedMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlushedMessageListener.class);

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public NewTopic flushed;

	@Autowired
	public ProductEventMessageProducer productEventMessageProducer;

	@Autowired
	ShutdownController shutdownController;
	
	@KafkaListener(topics = "#{__listener.topic}", containerFactory = "productPairKafkaListenerContainerFactory", batch = "true")
	public void productPairListener(Flushed flushed) {
		LOGGER.warn("Received flushed message: " + flushed);
		
		Report report = environment.getReport();
		report.setCountInsert(report.getCountInsert()+flushed.getCountInsert());
		report.setCountUpdate(report.getCountUpdate()+flushed.getCountUpdate());
		report.setCountDelete(report.getCountDelete()+flushed.getCountDelete());
		report.setCountError(report.getCountError()+flushed.getCountError());
		

//		int sumEvent = productEventMessageProducer.getCounter();
		int sumEvent = report.getSumEvent();
		int sumProcessed = flushed.getSumProcessed();
		LOGGER.warn("sumEvent    : " + sumEvent);
		LOGGER.warn("sumProcessed: " + sumProcessed);
		report.setSumProcessed(sumProcessed);
		
		BigDecimal temp = BigDecimal.valueOf(sumProcessed).divide(BigDecimal.valueOf(sumEvent), 2, RoundingMode.CEILING);
		long i = temp.multiply(BigDecimal.valueOf(100)).intValue();
		
		LOGGER.warn("i: " + i);

		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; j++) {
			sb.append("#");
		}
		System.out.print("[" + String.format("%-100s", sb.toString()) + "] " + i + "% ("+environment.getRequestid()+")" +"\r");

		if (i >= 100) {
			shutdownController.shutdownContext();
		}

	}

	public String getTopic() {
		return flushed.name();
	}

}