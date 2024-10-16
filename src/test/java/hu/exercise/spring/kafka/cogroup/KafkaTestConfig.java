package hu.exercise.spring.kafka.cogroup;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import hu.exercise.spring.kafka.config.KafkaSerdeConfig;

@Configuration
@EnableKafkaStreams
@EnableKafka
public class KafkaTestConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTestConfig.class);

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	@Autowired
	KafkaSerdeConfig kafkaSerdeConfig;

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public KafkaStreamsConfiguration kStreamsConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testStreams");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
				kafkaSerdeConfig.productEventSerde().getClass().getName());

		return new KafkaStreamsConfiguration(props);
	}

	@Bean
	public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
		return factoryBean -> {
			LOGGER.info("StreamsBuilderFactoryBeanConfigurer:" + factoryBean);
			factoryBean.setCleanupConfig(new CleanupConfig(true, true));
			factoryBean.getStreamsConfiguration().put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
			factoryBean.getStreamsConfiguration().put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
			factoryBean.setAutoStartup(false);
		};
	}

}