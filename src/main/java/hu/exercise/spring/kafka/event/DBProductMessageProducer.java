package hu.exercise.spring.kafka.event;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.input.Product;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import net.csini.spring.kafka.KafkaEntityObserver;

@Service
public class DBProductMessageProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBProductMessageProducer.class);

	@Autowired
	public KafkaEnvironment environment;

	@KafkaEntityObserver(entity = ReadedFromDBEvent.class)
	private Observer<ReadedFromDBEvent> readedFromDBEventObserver;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.ReadedFromDBEvent", description = "All the Product readed from DB."))
	public void sendMessages(Observable<Product> fromStream) throws IOException {
		LOGGER.info("sendMessages");
		Report report = environment.getReport();
		try {
			
			fromStream.map(p -> {

				report.setCountReadedFromDB(report.getCountReadedFromDB() + 1);
				report.setSumReaded(report.getSumReaded() + 1);

				LOGGER.info("sending product to readedFromDb: " + p);
				ReadedFromDBEvent event = new ReadedFromDBEvent(p.getId(), environment.getRequestid().toString(), p);
//				sendEvent(event);

//				CompletableFuture<SendResult<String, ProductEvent>> sendProductMessage = super.sendProductMessage(
//						event);
				
				return event;
			}).subscribe(readedFromDBEventObserver);
			
		} finally {
			LOGGER.warn("sending events to readedFromDb: " + report.getCountReadedFromDB());
		}
	}

}
