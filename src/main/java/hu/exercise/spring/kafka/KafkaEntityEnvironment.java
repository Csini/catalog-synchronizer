package hu.exercise.spring.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hu.exercise.spring.kafka.listener.FlushedMessageListener;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;
import net.csini.spring.kafka.config.KafkaEntityConfig;
import net.csini.spring.kafka.exception.KafkaEntityException;

@Configuration
@Getter
public class KafkaEntityEnvironment implements DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEntityEnvironment.class);

	@Autowired
	FlushedMessageListener flushedMessageListener;

	@Autowired
	ApplicationContext applicationContext;

	@Value(value = "${spring.kafka.bootstrap-servers:localhost:9092}")
	private List<String> bootstrapServers = new ArrayList<>(Collections.singletonList("localhost:9092"));

	private Disposable subscribed;

	@Bean
	public KafkaEntityConfig kafkaEntityConfig() throws KafkaEntityException {

		KafkaEntityConfig kafkaEntityConfig = new KafkaEntityConfig(applicationContext, bootstrapServers);
		kafkaEntityConfig.afterPropertiesSet();
		subscribed = flushedMessageListener.flushedListener();
		return kafkaEntityConfig;
	}

	@Override
	public void destroy() throws Exception {

		if (subscribed != null) {
			LOGGER.warn("Disposing flushObservable...");
			subscribed.dispose();
		}
	}

//	@Bean
//	public EmbeddedKafkaBroker embeddedKafkaBroker() {
////		@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092",
////				"offsets.topic.replication.factor=1", "offset.storage.replication.factor=1",
////				"transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1" })
//		EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaBroker(1);
//		embeddedKafkaBroker.brokerProperty("listeners", "PLAINTEXT://localhost:9092").brokerProperty("port", "9092")
//				.brokerProperty("offsets.topic.replication.factor", "1")
//				.brokerProperty("offset.storage.replication.factor", "1")
//				.brokerProperty("transaction.state.log.replication.factor", "1")
//				.brokerProperty("transaction.state.log.min.isr", "1");
//		embeddedKafkaBroker.afterPropertiesSet();
//		return embeddedKafkaBroker;
//	}
}
