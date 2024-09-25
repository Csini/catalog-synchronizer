package hu.exercise.spring.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.ShutdownController;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.topic.Flushed;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import net.csini.spring.kafka.KafkaEntityObservable;

@Service
public class FlushedMessageListener implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlushedMessageListener.class);

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	ShutdownController shutdownController;

	@KafkaEntityObservable(entity = Flushed.class)
	Observable<Flushed> flushedObservable;

	@AsyncListener(operation = @AsyncOperation(channelName = "hu.exercise.spring.kafka.event.Flushed"))
	public Disposable flushedListener(/* Flushed flushed */) {

		@NonNull
		Disposable subscribed = flushedObservable.subscribe((Flushed flushed) -> {
			LOGGER.info("Received flushed message: " + flushed);

			Report report = environment.getReport();
			report.setCountInsert(report.getCountInsert() + flushed.getCountInsert());
			report.setCountUpdate(report.getCountUpdate() + flushed.getCountUpdate());
			report.setCountDelete(report.getCountDelete() + flushed.getCountDelete());
			report.setCountError(report.getCountError() + flushed.getCountError());
//		report.setSumProcessed(flushed.getSumProcessed());

			long i = report.printProgressbar();

			if (i >= 100) {
				shutdownController.shutdownContext();
			}
		});
		return subscribed;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public void destroy() throws Exception {
		
	}

}