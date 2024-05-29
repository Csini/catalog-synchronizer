package hu.exercise.spring.kafka.cogroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.config.JAXBConfig;
import hu.exercise.spring.kafka.config.KafkaSerdeConfig;
import hu.exercise.spring.kafka.config.KafkaStreamsConfig;
import hu.exercise.spring.kafka.config.KafkaTopicConfig;
import hu.exercise.spring.kafka.event.ProductEvent;
import junit.framework.Assert;

@TestPropertySource("/application.yml")
//@SpringJUnitConfig
//@DirtiesContext
@SpringBootTest(classes = { KafkaTestConfig.class, KafkaSerdeConfig.class, KafkaStreamsConfig.class, KafkaTopicConfig.class, KafkaEnvironment.class,
		JAXBConfig.class, ProductServiceSpy.class, PlatformTransactionManagerSpy.class,
		AppContextRefreshedEventPropertiesPrinter.class })
@EmbeddedKafka(partitions = 1 /* , topics = {"#{kafkaTopicConfig.flushedName}"} */ )
public class KafkaStreamTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamTest.class);

	private TopologyTestDriver testDriver;
	private KeyValueStore<String, Long> store;

	@Autowired
	StreamsBuilderFactoryBean factory;

//	private StringDeserializer stringDeserializer = new StringDeserializer();
//	private LongDeserializer longDeserializer = new LongDeserializer();
//	private ConsumerRecordFactory<String, Long> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(),
//			new LongSerializer());

	@Autowired
	private KafkaTemplate<String, ProductEvent> kafkaTemplate;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	@Autowired
	KafkaStreamsConfig streamsConfig;

	@Autowired
	KafkaTopicConfig kafkaTopicConfig;
	
	@Autowired
	KafkaSerdeConfig kafkaSerdeConfig;
	
	@Autowired
	KafkaEnvironment environment;

	@Test
	public void test_RollupStream() {

		embeddedKafka.addTopics(kafkaTopicConfig.readedFromDb(), kafkaTopicConfig.validProduct(),
				kafkaTopicConfig.invalidProduct(), kafkaTopicConfig.flushed(), kafkaTopicConfig.productTopic(),
				kafkaTopicConfig.dbEventTopic(), kafkaTopicConfig.runs());

		try (Consumer<String, Flushed> flushConsumer = createConsumer();) {

			this.embeddedKafka.consumeFromAnEmbeddedTopic(flushConsumer, kafkaTopicConfig.getFlushedName());

			streamsConfig.productRollupStream();
			factory.setCleanupConfig(new CleanupConfig(true, false));
			factory.setStreamsUncaughtExceptionHandler(ex -> {
				LOGGER.error("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread", ex);
				Assertions.fail(ex);
				return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
			});

			environment.getReport().setSumReaded(1);
			
			// TODO
			ProductEvent productEvent = new ProductEvent();
			productEvent.setId("1");
			productEvent.setRequestid(environment.getRequestid());
			this.kafkaTemplate.send(kafkaTopicConfig.getProductTopicName(), productEvent);

			factory.start();
			
			

			ConsumerRecords<String, Flushed> flushRecords = KafkaTestUtils.getRecords(flushConsumer);

			List<Flushed> flushValues = new ArrayList<>();
			flushRecords.forEach(record -> flushValues.add(record.value()));

			// TODO assertions
//			Assertions.assertNotEquals(0, flushValues.size());
			//no error
			Assertions.assertEquals(0, flushValues.size());
			
//		flushConsumer.close();
		}
	}

	private Consumer<String, Flushed> createConsumer() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false",
				this.embeddedKafka);
		consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10000);

		DefaultKafkaConsumerFactory<String, Flushed> kafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
				consumerProps, Serdes.String().deserializer(), kafkaSerdeConfig.flushedSerde().deserializer());
		return kafkaConsumerFactory.createConsumer();
	}

//	@BeforeEach
//	public void setup() {
////		Topology topology = new Topology();
////		
////		topology.
////		topology.addSource("sourceProcessor", "input-topic");
////		topology.addProcessor("aggregator", new CustomMaxAggregatorSupplier(), "sourceProcessor");
////		topology.addStateStore(
////				Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("aggStore"), Serdes.String(), Serdes.Long())
////						.withLoggingDisabled(), // need to disable logging to allow store pre-populating
////				"aggregator");
////		topology.addSink("sinkProcessor", "result-topic", "aggregator");
////
////		// setup test driver
////		Properties props = new Properties();
////		props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "maxAggregation");
////		props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
////		props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
////		props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
////		testDriver = new TopologyTestDriver(topology, props);
//
//		// pre-populate store
//		store = testDriver.getKeyValueStore("aggStore");
//		store.put("a", 21L);
//	}
//
//	@AfterEach
//	public void tearDown() {
//		testDriver.close();
//	}

//	@Test
//	public void shouldFlushStoreForFirstInput() {
//		testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
//		OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a",
//				21L);
//		Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
//	}
//
//	@Test
//	public void shouldNotUpdateStoreForSmallerValue() {
//		testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
//		Assert.assertThat(store.get("a"), equalTo(21L));
//		OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a",
//				21L);
//		Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
//	}
}
