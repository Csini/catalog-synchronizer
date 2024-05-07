package hu.exercise.spring.kafka.cogroup;

import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.event.ProductEvent;

//@Component
public class CogroupingStreams {

	private static final Logger LOGGER = LoggerFactory.getLogger(CogroupingStreams.class);

	@Autowired
	public NewTopic mergedProductEvents;

	@Autowired
	public NewTopic readedFromDb;

	@Autowired
	public NewTopic validProduct;

//	public KafkaStreams getStreams(Properties allProps) {
//		Topology topology = buildTopology(allProps);
//
//		final KafkaStreams streams = new KafkaStreams(topology, allProps);
//
////	        final CountDownLatch latch = new CountDownLatch(1);
////
////	        // Attach shutdown handler to catch Control-C.
////	        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
////	            @Override
////	            public void run() {
////	                streams.close(Duration.ofSeconds(5));
////	                latch.countDown();
////	            }
////	        });
////
////	        try {
////	            streams.start();
////	            latch.await();
////	        } catch (Throwable e) {
////	            System.exit(1);
////	        }
////	        System.exit(0);
//
//		return streams;
//	}

//	@Autowired
//    public void buildPipeline(StreamsBuilder builder) {
////	private Topology buildTopology(Properties allProps) {
////		final StreamsBuilder builder = new StreamsBuilder();
////        final String readedFromDbTopic = allProps.getProperty("app-one.topic.name");
//		final String readedFromDbTopic = readedFromDb.name();
//		final String validProductTopic = validProduct.name();
//
//		final String totalResultOutputTopic = mergedProductEvents.name();
//
//		final Serde<String> stringSerde = Serdes.String();
//		final Serde<ProductEvent> productEventSerde = CustomSerdes.ProductEvent();
//		final Serde<ProductRollup> productRollupSerde = CustomSerdes.ProductRollup();
//
//		final KStream<String, ProductEvent> readedStream = builder.stream(readedFromDbTopic,
//				Consumed.with(stringSerde, productEventSerde));
//		final KStream<String, ProductEvent> validStream = builder.stream(validProductTopic,
//				Consumed.with(stringSerde, productEventSerde));
//
//		final Aggregator<String, ProductEvent, ProductRollup> productAggregator = new ProductAggregator();
//
//		final KGroupedStream<String, ProductEvent> readedGrouped = readedStream
//				.groupByKey();
////				.selectKey((key, value) -> "DB" + value.getProduct().getFilename()).groupByKey();
////				.map((key, p) -> new KeyValue<>(p.getProduct().getId(), p))
////				.groupByKey(Grouped.with("GROUP_BY_DB", Serdes.String(), CustomSerdes.ProductEvent()));
//		final KGroupedStream<String, ProductEvent> validGrouped = validStream
//				.groupByKey();
////				.selectKey((key, value) -> "VALID" + value.getProduct().getFilename()).groupByKey();
////				.map((key, p) -> new KeyValue<>(p.getProduct().getId(), p)).filter((key, v) -> {
////
////					LOGGER.info("xxxxxxx " + key + ": " + v);
////					return true;
////				}).groupByKey(Grouped.with("GROUP_BY_VALID", Serdes.String(), CustomSerdes.ProductEvent()));
//
//		
//		KStream<String, ProductRollup> stream = readedGrouped.cogroup(productAggregator).cogroup(validGrouped, productAggregator)
//		.aggregate(() -> new ProductRollup(), Materialized.with(Serdes.String(), productRollupSerde)).toStream();
//		stream.to(totalResultOutputTopic, Produced.with(stringSerde, productRollupSerde));
//
////		stream.foreach((key, rollup) -> {rollup.getProducts().values()});
//		
////		stream.map((key, rollup) -> {
////			return rollup.getProducts().values().map(pair -> new KeyValue<String, ProductPair>(pair.getId(), pair)).collect(Collectors.toList());
////			});
////		final KStream<String, ProductPair> valami = builder.stream("valami",
////				Consumed.with(stringSerde, productPairtSerde));
////
////stream.foreach((key, rollup) -> {
////	("valami",rollup.getProducts().values().stream().map(pair -> new KeyValue<K, V>(pair.getAction(), pair)));
////});
//		
////		return builder.build();
//	}

//    public Properties loadEnvProperties(String fileName) throws IOException {
//        final Properties allProps = new Properties();
//        final FileInputStream input = new FileInputStream(fileName);
//        allProps.load(input);
//        input.close();
//
//        return allProps;
//    }
}
