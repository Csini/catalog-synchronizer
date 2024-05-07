package hu.exercise.spring.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.KafkaStreamBrancher;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.cogroup.ProductAggregator;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.input.Product;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaStreamsConfig {

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

//    @Value("${kafka.topics.iot}")
//    private String iotTopicName;

	@Autowired
	public NewTopic productRollup;

	@Autowired
	public NewTopic readedFromDb;

	@Autowired
	public NewTopic validProduct;

	@Autowired
	public KafkaEnvironment environment;

	@Bean
	public Serde<ProductRollup> productRollupSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductRollup.class));
	}

	@Bean
	public Serde<ProductEvent> productEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductEvent.class));
	}

	@Bean
	public Serde<Product> productSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Product.class));
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

	@Bean
	public KStream<String, ProductRollup> productRollupStream(StreamsBuilder builder) {
		final String readedFromDbTopic = readedFromDb.name();
		final String validProductTopic = validProduct.name();

		final String totalResultOutputTopic = productRollup.name();

		final Serde<String> stringSerde = Serdes.String();

		Serde<ProductEvent> productEventSerde = productEventSerde();
		final KStream<String, ProductEvent> readedStream = builder.stream(readedFromDbTopic,
				Consumed.with(stringSerde, productEventSerde));
		final KStream<String, ProductEvent> validStream = builder.stream(validProductTopic,
				Consumed.with(stringSerde, productEventSerde));

		final Aggregator<String, ProductEvent, ProductRollup> productAggregator = new ProductAggregator(
				environment.getRequestid());

		final KGroupedStream<String, ProductEvent> readedGrouped = readedStream.groupByKey();
		final KGroupedStream<String, ProductEvent> validGrouped = validStream.groupByKey();

		Serde<ProductRollup> productRollupSerde = productRollupSerde();
		KStream<String, ProductRollup> stream = readedGrouped.cogroup(productAggregator)
				.cogroup(validGrouped, productAggregator).aggregate(() -> new ProductRollup(environment.getRequestid()),
						Materialized.with(Serdes.String(), productRollupSerde))
				.toStream();

		stream.to(totalResultOutputTopic, Produced.with(stringSerde, productRollupSerde));

		Serde<Product> productSerde = productSerde();
		new KafkaStreamBrancher<String, ProductRollup>()
				.branch((key, value) -> Action.INSERT.equals(value.getPair().getAction()),
						(ks) -> ks.map(
								(key, value) -> new KeyValue<String, Product>(key, value.getPair().getReadedFromFile()))
								.to("product-" + Action.INSERT, Produced.with(stringSerde, productSerde)))
				.branch((key, value) -> Action.UPDATE.equals(value.getPair().getAction()),
						(ks) -> ks.map(
								(key, value) -> new KeyValue<String, Product>(key, value.getPair().getReadedFromFile()))
								.to("product-" + Action.UPDATE, Produced.with(stringSerde, productSerde)))
				.branch((key, value) -> Action.DELETE.equals(value.getPair().getAction()),
						(ks) -> ks.map(
								(key, value) -> new KeyValue<String, Product>(key, value.getPair().getReadedFromDb()))
								.to("product-" + Action.DELETE, Produced.with(stringSerde, productSerde)))
				.defaultBranch(ks -> ks.to("product-UNKNOWN")).onTopOf(stream);

		return stream;
	}

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
}
