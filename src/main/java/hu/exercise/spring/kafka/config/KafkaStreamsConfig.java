package hu.exercise.spring.kafka.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.transaction.PlatformTransactionManager;

import com.codahale.metrics.MetricRegistry;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.CustomDBWriter;
import hu.exercise.spring.kafka.cogroup.CustomProductPairAggregator;
import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.cogroup.ProductPair;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;
import jakarta.annotation.PostConstruct;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaStreamsConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsConfig.class);

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${aggregateWindowInSec}")
	private int aggregateWindowInSec;

	@Value(value = "${flushSize}")
	private int flushSize;
	
	@Value(value = "${store.name.productPair}")
	private String productPairStoreName ;

	@Autowired
	public KafkaTopicConfig kafkaTopicConfig;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public StreamsBuilder builder;

	@Autowired
	private ProductService productService;

	@Autowired
	private PlatformTransactionManager txManager;

	@Autowired
	private MetricRegistry metrics;

	@Autowired
	KafkaSerdeConfig kafkaSerdeConfig;

	public KStream<String, Flushed> productRollupStream() {

		final Serde<String> stringSerde = Serdes.String();

		final Serde<ProductEvent> productEventSerde = kafkaSerdeConfig.productEventSerde();

		KStream<String, Flushed> lastStream = builder
				.stream(kafkaTopicConfig.getProductTopicName(), Consumed.with(stringSerde, productEventSerde))
				.filter((key, productEvent) -> environment.getRequestid().toString().equals(key))
				.process(() -> new CustomProductPairAggregator(metrics, aggregateWindowInSec, flushSize, productPairStoreName,
						environment), productPairStoreName)
				.process(() -> new CustomDBWriter(environment.getReport().getSumEvent(), environment, productService,
						txManager));

		lastStream.to(kafkaTopicConfig.getFlushedName(), Produced.with(stringSerde, kafkaSerdeConfig.flushedSerde()));

		return lastStream;
	}

	public void addStateStore() {
		
		final Serde<ProductPair> productPairSerde = kafkaSerdeConfig.productPairSerde();
		final Serde<String> stringSerde = Serdes.String();
		
		String stateStoreName = productPairStoreName /*+ "-" + environment.getRequestid().toString()*/;

		StoreBuilder<KeyValueStore<String, ProductPair>> keyValueStoreBuilder = Stores.keyValueStoreBuilder(
				Stores.persistentTimestampedKeyValueStore(stateStoreName), stringSerde, productPairSerde);
		builder.addStateStore(keyValueStoreBuilder);
	}
	
	@PostConstruct
	public void postConstruct() {
		addStateStore();
		productRollupStream();
	}
}
