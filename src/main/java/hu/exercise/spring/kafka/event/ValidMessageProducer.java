package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.topic.ValidProductEvent;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import net.csini.spring.kafka.KafkaEntityObserver;

@Service
public class ValidMessageProducer {

	@KafkaEntityObserver(entity = ValidProductEvent.class)
	private Observer<ValidProductEvent> validProductEventObserver;

	@Autowired
	public KafkaEnvironment environment;

	@AsyncPublisher(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.ValidProductEvent}", description = "All the valid Products readed by the request from the input TSV."))
	public void sendEvent(Observable<Product> published) {
		
		published.map(bean -> new ValidProductEvent(bean.getId(), environment.getRequestid().toString(), bean))
				.subscribe(validProductEventObserver);
		

	}
}
