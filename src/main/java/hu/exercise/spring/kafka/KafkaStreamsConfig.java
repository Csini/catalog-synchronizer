package hu.exercise.spring.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
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

import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.cogroup.ProductAggregator;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.event.ProductEvent;

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

	@Bean
	public Serde<ProductRollup> productRollupSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductRollup.class));
	}

	@Bean
	public Serde<ProductEvent> productEventSerde() {
		return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(ProductEvent.class));
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

		final KStream<String, ProductEvent> readedStream = builder.stream(readedFromDbTopic,
				Consumed.with(stringSerde, productEventSerde()));
		final KStream<String, ProductEvent> validStream = builder.stream(validProductTopic,
				Consumed.with(stringSerde, productEventSerde()));

		final Aggregator<String, ProductEvent, ProductRollup> productAggregator = new ProductAggregator();

		final KGroupedStream<String, ProductEvent> readedGrouped = readedStream.groupByKey();
		final KGroupedStream<String, ProductEvent> validGrouped = validStream.groupByKey();

		KStream<String, ProductRollup> stream = readedGrouped.cogroup(productAggregator)
				.cogroup(validGrouped, productAggregator)
				.aggregate(() -> new ProductRollup(), Materialized.with(Serdes.String(), productRollupSerde()))
				.toStream();
		
		stream.to(totalResultOutputTopic, Produced.with(stringSerde, productRollupSerde()));

		new KafkaStreamBrancher<String, ProductRollup>()
				.branch((key, value) -> Action.INSERT.equals(value.getPair().getAction()), (ks) -> ks.to("product-" + Action.INSERT))
				.branch((key, value) -> Action.UPDATE.equals(value.getPair().getAction()), (ks) -> ks.to("product-" + Action.UPDATE))
				.branch((key, value) -> Action.DELETE.equals(value.getPair().getAction()), (ks) -> ks.to("product-" + Action.DELETE))
				.defaultBranch(ks -> ks.to("product-UNKNOWN"))
				.onTopOf(stream);

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
