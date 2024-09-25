package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import net.csini.spring.kafka.KafkaEntityObserver;

@Service
public class InvalidMessageProducer {

	@KafkaEntityObserver(entity = ProductErrorEvent.class)
	private Observer<ProductErrorEvent> productErrorEventObserver;
	
	@Autowired
	public KafkaEnvironment environment;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.ProductErrorEvent", description = "All the invalid Products readed from the input TSV."))
	public void sendEvent(ProductErrorEvent productErrorEvent) {

		environment.getReport().setCountReadedFromTsvInvalid(environment.getReport().getCountReadedFromTsvInvalid()+1);
		
		Observable.just(productErrorEvent).subscribe(productErrorEventObserver);
	}
}
