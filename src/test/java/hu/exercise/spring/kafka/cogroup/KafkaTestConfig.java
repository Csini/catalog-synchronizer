package hu.exercise.spring.kafka.cogroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import hu.exercise.spring.kafka.config.KafkaSerdeConfig;
import hu.exercise.spring.kafka.event.ProductEvent;

@Configuration
@EnableKafkaStreams
public class KafkaTestConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTestConfig.class);

	@Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
	private String brokerAddresses;
	
	@Autowired
	KafkaSerdeConfig kafkaSerdeConfig;

	@Bean
	public ProducerFactory<String, ProductEvent> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(this.brokerAddresses);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return producerProps;
	}

	@Bean
	public KafkaTemplate<?, ?> kafkaTemplate() {
		KafkaTemplate<String, ProductEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory());
//		kafkaTemplate.setDefaultTopic(kafkaTopicConfig.getProductTopicName());
		return kafkaTemplate;
	}

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public KafkaStreamsConfiguration kStreamsConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testStreams");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddresses);
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, kafkaSerdeConfig.productEventSerde().getClass().getName());

		return new KafkaStreamsConfiguration(props);
	}

	@Bean
	public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
		return factoryBean -> {
//	    	new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config), new CleanupConfig(true, true));
			LOGGER.info("StreamsBuilderFactoryBeanConfigurer:" + factoryBean);
			factoryBean.setCleanupConfig(new CleanupConfig(true, true));
			factoryBean.getStreamsConfiguration().put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
			factoryBean.getStreamsConfiguration().put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
			factoryBean.setAutoStartup(false);
//			factoryBean.getKafkaStreams().cleanUp();
//			factoryBean.getKafkaStreams().
		};
	}

//		@Bean
//		public KStream<String, String> trueFalseStream(StreamsBuilder streamsBuilder) {
//			return new KafkaStreamBrancher<String, String>()
//					.branch((key, value) -> String.valueOf(true).equals(value),
//							ks -> ks.to(TRUE_TOPIC, Produced.with(Serdes.String(), Serdes.String())))
//					.branch((key, value) -> String.valueOf(false).equals(value),
//							ks -> ks.to(FALSE_TOPIC, Produced.with(Serdes.String(), Serdes.String())))
//					.onTopOf(streamsBuilder.stream(TRUE_FALSE_INPUT_TOPIC,
//							Consumed.with(Serdes.String(), Serdes.String())));
//		}

}