package hu.exercise.spring.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
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
import com.codahale.metrics.Timer;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.CustomDBWriter;
import hu.exercise.spring.kafka.cogroup.CustomProductPairAggregator;
import hu.exercise.spring.kafka.cogroup.Flushed;
import hu.exercise.spring.kafka.cogroup.ProductPair;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.cogroup.Report;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaStreamsConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsConfig.class);

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${productPair.store.name}")
	private String productPairStoreName;
	
	@Value(value = "${aggregateWindowInSec}")
	private int aggregateWindowInSec;

	@Value(value = "${flushSize}")
	private int flushSize;
	
	@Autowired
	public NewTopic flushed;

	@Autowired
	public NewTopic productTopic;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public StreamsBuilder builder;

	@Autowired
	private ProductService productService;
	
	@Autowired
	private PlatformTransactionManager txManager;
	
	@Autowired
	private  MetricRegistry metrics;
	
	@Bean
	public Serde<ProductRollup> productRollupSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductRollup.class));
	}

	@Bean
	public Serde<ProductPair> productPairSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductPair.class));
	}

	@Bean
	public Serde<ProductEvent> productEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductEvent.class));
	}

	@Bean
	public Serde<Product> productSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Product.class));
	}

	@Bean
	public Serde<Flushed> flushedSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Flushed.class));
	}

//	@Value(value = "${spring.kafka.streams.state.dir}")
//    private String stateStoreLocation;

//	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
//	KafkaStreamsConfiguration kStreamsConfig() {
//		Map<String, Object> props = new HashMap<>();
//		props.put(APPLICATION_ID_CONFIG, "product-input");
//		props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//		props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//		props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//		// configure the state location to allow tests to use clean state for every run
////        props.put(STATE_DIR_CONFIG, stateStoreLocation);
//
//		return new KafkaStreamsConfiguration(props);

//	}

//	@Bean
//	public KStream<String, ProductRollup> productRollupStream(StreamsBuilder builder) {
	public KStream<String, Flushed> productRollupStream() {

		final Serde<String> stringSerde = Serdes.String();
//		final Serde<Windowed<String>> windowedSerde = WindowedSerdes.sessionWindowedSerdeFrom(String.class);

		Serde<ProductEvent> productEventSerde = productEventSerde();
		Serde<ProductPair> productPairSerde = productPairSerde();
//		final KStream<String, ProductEvent> readedStream = builder
//				.stream(readedFromDbTopic, Consumed.with(stringSerde, productEventSerde))
//				.filter((key, value) -> environment.getRequestid().equals(value.getRequestid()));
//		final KStream<String, ProductEvent> validStream = builder
//				.stream(validProductTopic, Consumed.with(stringSerde, productEventSerde))
//				.filter((key, value) -> environment.getRequestid().equals(value.getRequestid()));

//		final Aggregator<String, ProductEvent, ProductRollup> productAggregator = new ProductAggregator(
//				environment.getRequestid());

//		final KGroupedStream<String, ProductEvent> readedGrouped = readedStream.groupByKey();
//		final KGroupedStream<String, ProductEvent> validGrouped = validStream.groupByKey();
//
//		Serde<ProductRollup> productRollupSerde = productRollupSerde();
//		KTable<String, ProductRollup> stream = readedGrouped.cogroup(productAggregator)
//				.cogroup(validGrouped, productAggregator)
////				.aggregate(() -> new ProductRollup())
//				.aggregate(() -> new ProductRollup(), Materialized.with(stringSerde, productRollupSerde))
////				.suppress(Suppressed.untilsTimeLimit(Duration.ofSeconds(60), Suppressed.BufferConfig.unbounded()))
//		;

//		final Topology topology = builder.build();
//		topology.addSource("sourceProcessor", stringSerde.deserializer(), productEventSerde.deserializer(), productTopic.name());
//		topology.addProcessor("aggregator", new CustomMaxAggregatorSupplier(), "sourceProcessor");
//		topology.addStateStore(
//				Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("aggStore"), stringSerde, productEventSerde)
//						.withLoggingDisabled(), // need to disable logging to allow store pre-populating
//				"aggregator");
//		topology.addSink("sinkProcessor", productRollup.name(),stringSerde.serializer(), productEventSerde.serializer(),"aggregator");

//		final KStream<String, ProductEvent> productEventStream = builder
//				.stream(productTopic.name(), Consumed.with(stringSerde, productEventSerde))
//				.filter((key, value) -> environment.getRequestid().equals(value.getRequestid()));
//
//		KTable<Windowed<String>, ProductRollup> last = productEventStream.groupByKey()
//				.aggregate(() -> new ProductRollup(), productAggregator,
//						Materialized.with(stringSerde, productRollupSerde))
//				.toStream().groupByKey().windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(3)))
//				.reduce((value1, value2) -> value2, Materialized.with(stringSerde, productRollupSerde))
//				.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()));

//		stream.split().branch((key, value) -> true).;

//		KGroupedStream<String, ProductRollup> u = stream.toStream()
//				.filter((key, value) -> value.getProductrollupid().startsWith("" + environment.getRequestid()))
//				.groupBy((key, value) -> value.getProductrollupid())
		;
//				.windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofSeconds(60)));
//				.windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60)));

//		KTable<Windowed<String>, ProductRollup> last = stream
////				.filter((key, value) -> value.getProductrollupid().startsWith("" + environment.getRequestid()))
//				.toStream()
////				.selectKey((key, value) -> value.getProductrollupid())
//				.groupByKey()
//			    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(20)))
//			    .reduce((value1, value2) -> value2, Materialized.with(stringSerde, productRollupSerde))
//			    .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()));
//				// .emitStrategy(EmitStrategy.onWindowClose())
//				.reduce((prod1, prod2) -> {
//					if(prod1.getCreated().after(prod2.getCreated())) {
//						return prod1;
//					}
//					return prod2;
//				})
//				.aggregate()
//				 .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().withLoggingDisabled()))
//				.suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(60), Suppressed.BufferConfig.unbounded()));
//				.toStream();
//		.to(environment.getRequestid()+ "-" + totalResultOutputTopic
//				/*, Produced.with(stringSerde, productRollupSerde)*/);
//
//		KStream<Windowed<String>, ProductRollup> lastStream = last.toStream();
//

		String stateStoreName = productPairStoreName + "-" + environment.getRequestid().toString();

		StoreBuilder<KeyValueStore<String, ProductPair>> keyValueStoreBuilder = Stores
				.keyValueStoreBuilder(Stores.persistentTimestampedKeyValueStore(stateStoreName), stringSerde, productPairSerde);
		builder.addStateStore(keyValueStoreBuilder);

		KStream<String, Flushed> lastStream = builder
				.stream(productTopic.name(), Consumed.with(stringSerde, productEventSerde))
				.filter((key, productEvent) -> environment.getRequestid().toString().equals(key))
//				.filter((key, productEvent) -> environment.getRequestid().equals(productEvent.getRequestid()))
				.process(() -> new CustomProductPairAggregator(metrics, aggregateWindowInSec,flushSize, stateStoreName, environment), stateStoreName)
				.process(() -> new CustomDBWriter(environment.getReport().getSumEvent(),environment, productService, txManager));

//		KStream<String, ProductRollup> lastStream = builder.stream(productRollup.name(),
//				Consumed.with(stringSerde, productRollupSerde));

		lastStream.to(flushed.name(), Produced.with(stringSerde, flushedSerde()));

		Serde<Product> productSerde = productSerde();

//		lastStream
////		.repartition(
////				Repartitioned.as(/*environment.getRequestid() + "-" + */totalResultOutputTopic).with(stringSerde,productRollupSerde)
////				)
//				.split()
//				.branch((key, value) -> Action.INSERT.equals(value.getPair().getAction()),
//						Branched.withConsumer((KStream<String, ProductRollup> ks) -> ks
//								.mapValues((ProductRollup value) -> value.getPair().getReadedFromFile().getProduct())
////						.repartition(environment.getRequestid()+ "-" + "product-" + Action.INSERT, Produced.with(windowedSerde, productSerde))
//								.to(environment.getRequestid() + "-" +"product-" + Action.INSERT, Produced.with(stringSerde, productSerde))))
//				.branch((key, value) -> Action.UPDATE.equals(value.getPair().getAction()),
//						Branched.withConsumer((KStream<String, ProductRollup> ks) -> ks
//								.mapValues((ProductRollup value) -> value.getPair().getReadedFromFile().getProduct())
////						.repartition(environment.getRequestid()+ "-" + "product-" + Action.UPDATE, Produced.with(windowedSerde, productSerde))
//								.to(environment.getRequestid() + "-" +"product-" + Action.UPDATE, Produced.with(stringSerde, productSerde))))
//				.branch((key, value) -> Action.DELETE.equals(value.getPair().getAction()),
//						Branched.withConsumer((KStream<String, ProductRollup> ks) -> ks
//								.mapValues((ProductRollup value) -> value.getPair().getReadedFromDb().getProduct())
////						.repartition(Repartitioned.as(environment.getRequestid()+ "-" + "product-" + Action.DELETE).with(stringSerde, productSerde))
//								.to(environment.getRequestid() + "-" +"product-" + Action.DELETE, Produced.with(stringSerde, productSerde))))
//				.noDefaultBranch();

//		new KafkaStreamBrancher<String, ProductRollup>()
//				.branch((key, value) -> Action.INSERT.equals(value.getPair().getAction()),
//						(ks) -> ks.map((key, value) -> new KeyValue<String, Product>(key,
//								value.getPair().getReadedFromFile().getProduct())).to("product-" + Action.INSERT,
//										Produced.with(stringSerde, productSerde)))
//				.branch((key, value) -> Action.UPDATE.equals(value.getPair().getAction()),
//						(ks) -> ks
//								.map((key, value) -> new KeyValue<String, Product>(key,
//										value.getPair().getReadedFromFile().getProduct()))
//								.to("product-" + Action.UPDATE, Produced.with(stringSerde, productSerde)))
//				.branch((key, value) -> Action.DELETE.equals(value.getPair().getAction()),
//						(ks) -> ks
//								.map((key, value) -> new KeyValue<String, Product>(key,
//										value.getPair().getReadedFromDb().getProduct()))
//								.to("product-" + Action.DELETE, Produced.with(stringSerde, productSerde)))
//				.defaultBranch(ks -> ks.to("product-UNKNOWN")).onTopOf(u);

		// TODO
//		builder.stream("product-" + Action.UPDATE, Consumed.with(windowedSerde, productSerde))
//				.to(environment.getRequestid() + "-" + "product-" + Action.UPDATE);
//		builder.stream("product-" + Action.INSERT, Consumed.with(windowedSerde, productSerde))
//				.to(environment.getRequestid() + "-" + "product-" + Action.INSERT);
//		builder.stream("product-" + Action.DELETE, Consumed.with(windowedSerde, productSerde))
//				.to(environment.getRequestid() + "-" + "product-" + Action.DELETE);

		return lastStream;
	}

//	@Bean
//	public KStream<String, Product> updateStream(StreamsBuilder builder) {
//		Serde<Product> productSerde = productSerde();
//		final Serde<String> stringSerde = Serdes.String();
//
//	}
//	
//	@Bean
//	public KStream<String, ProductRollup> insertStream(StreamsBuilder builder) {
//		Serde<Product> productSerde = productSerde();
//		final Serde<String> stringSerde = Serdes.String();
//	
//	}
//	
//	@Bean
//	public KStream<String, ProductRollup> deleteStream(StreamsBuilder builder) {
//		Serde<Product> productSerde = productSerde();
//		final Serde<String> stringSerde = Serdes.String();
//	
//	}

//	@Bean
//    public KStream<String, ProductRollup> productRollupBrancher(StreamsBuilder streamsBuilder) {
//        KStream<String, ProductRollup> stream = streamsBuilder.stream(productRollup.name(), Consumed.with(Serdes.String(), iotSerde()));
//
//        new KafkaStreamBrancher<String, ProductRollup>()
//                .branch((key, value) -> "temp".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_temp"))
//                .branch((key, value) -> "move".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_move"))
//                .branch((key, value) -> "hum".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_hum"))
//                .defaultBranch(ks -> ks.to(String.format("%s_unknown", iotTopicName)))
//                .onTopOf(stream);
//
//        return stream;
//    }

//	@Bean
//	public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
//		return factoryBean -> {
////	    	new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config), new CleanupConfig(true, true));
//			LOGGER.info("StreamsBuilderFactoryBeanConfigurer:" + factoryBean);
//			factoryBean.setCleanupConfig(new CleanupConfig(true, true));
//			factoryBean.getStreamsConfiguration().put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
//			factoryBean.getStreamsConfiguration().put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
////			factoryBean.setAutoStartup(false);
////			factoryBean.getKafkaStreams().cleanUp();
////			factoryBean.getKafkaStreams().
//		};
//	}

//	@Bean
//	KafkaStreams kafkaStreams(StreamsBuilder builder, KafkaStreamsConfiguration streamsConfig) {
//		KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), streamsConfig.asProperties());
//		LOGGER.info("----Its is started----");
//		kafkaStreams.cleanUp();
//		kafkaStreams.start();
//		return kafkaStreams;
//	}

//	@PostConstruct
//	public void postconstruct() {
//		LOGGER.info("postconstruct");
//		
//		factory.getKafkaStreams().cleanUp();
//	}
//
//	@PreDestroy
//	public void predestroy() {
//		LOGGER.info("predestroy");
//		streams.cleanUp();
//		streams.close();
//	}

}
