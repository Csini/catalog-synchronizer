package hu.exercise.spring.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.input.Product;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import net.csini.spring.kafka.KafkaEntityObserver;

@Service
public class DBEventMessageProducer {

	@KafkaEntityObserver(entity = DBEvent.class)
	private Observer<DBEvent> dbEventObserver;

	@Autowired
	public KafkaEnvironment environment;
	
	@AsyncPublisher(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.DBEvent", description = "All the DB Actions done by the request."))
	public void sendAll(@Payload Iterable<Product> saveAll, Action action) {

		Observable.fromIterable(saveAll).map(p -> new DBEvent(environment.getRequestid().toString(), p.getId(), action))
				.subscribe(dbEventObserver);

	}
}
