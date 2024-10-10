package hu.exercise.spring.kafka.event;

import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Run;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import net.csini.spring.kafka.KafkaEntityObserver;

@Service
public class RunMessageProducer {

	@KafkaEntityObserver(entity = Run.class)
	private Observer<Run> runObserver;
	
	@AsyncPublisher(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.Run", description = "All the Runs started from catalog-syncronizer."))
	public void sendRunMessage(Run run) {
		Observable.just(run).subscribe(runObserver);
	}
}
