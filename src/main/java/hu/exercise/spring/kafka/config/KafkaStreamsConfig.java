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
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.transaction.PlatformTransactionManager;

import com.codahale.metrics.MetricRegistry;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.CustomDBWriter;
import hu.exercise.spring.kafka.cogroup.CustomProductPairAggregator;
import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.cogroup.ProductPair;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.ReadedFromDBEvent;
import hu.exercise.spring.kafka.event.Source;
import hu.exercise.spring.kafka.event.ValidProductEvent;
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
	private String productPairStoreName;

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
		
		final Serde<ValidProductEvent> validProductEventSerde = kafkaSerdeConfig.validProductEventSerde();
		
		KStream<String, ProductEvent> tsvValidStream = builder
				.stream(ValidProductEvent.class.getName(), Consumed.with(stringSerde, validProductEventSerde))
				.filter((key, productEvent) -> environment.getRequestid().toString().equals(productEvent.getRequestid()))
				.mapValues(validProductEvent -> new ProductEvent(validProductEvent.getId(),
						validProductEvent.getRequestid(), Source.TSV, validProductEvent.getProduct()));
		
		tsvValidStream.to(ProductEvent.class.getName(),
				Produced.with(stringSerde, kafkaSerdeConfig.productEventSerde()));


		final Serde<ProductEvent> productEventSerde = kafkaSerdeConfig.productEventSerde();

		final Serde<ReadedFromDBEvent> readedFromDBEventSerde = kafkaSerdeConfig.readedFromDBEventSerde();
		
		KStream<String, ProductEvent> dbStream = builder
				.stream(ReadedFromDBEvent.class.getName(), Consumed.with(stringSerde, readedFromDBEventSerde))
				.filter((key, productEvent) -> environment.getRequestid().toString().equals(productEvent.getRequestid()))
				.mapValues(readedFromDBEvent -> new ProductEvent(readedFromDBEvent.getId(),
						readedFromDBEvent.getRequestid(), Source.DB, readedFromDBEvent.getProduct()));
		
		dbStream.to(ProductEvent.class.getName(),
				Produced.with(stringSerde, kafkaSerdeConfig.productEventSerde()));

		KStream<String, Flushed> lastStream = builder
				.stream(ProductEvent.class.getName(), Consumed.with(stringSerde, productEventSerde))
				.process(() -> new CustomProductPairAggregator(metrics, aggregateWindowInSec, flushSize,
						productPairStoreName, environment), productPairStoreName)
				.process(() -> new CustomDBWriter(environment, productService, txManager));

		lastStream.to(Flushed.class.getName(), Produced.with(stringSerde, kafkaSerdeConfig.flushedSerde()));

		return lastStream;
	}

	public void addStateStore() {

		final Serde<ProductPair> productPairSerde = kafkaSerdeConfig.productPairSerde();
		final Serde<String> stringSerde = Serdes.String();

		String stateStoreName = productPairStoreName /* + "-" + environment.getRequestid().toString() */;

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
