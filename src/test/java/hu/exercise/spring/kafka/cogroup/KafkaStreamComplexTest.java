package hu.exercise.spring.kafka.cogroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.config.InputOutputConfig;
import hu.exercise.spring.kafka.config.JAXBConfig;
import hu.exercise.spring.kafka.config.KafkaSerdeConfig;
import hu.exercise.spring.kafka.config.KafkaStreamsConfig;
import hu.exercise.spring.kafka.event.DBEvent;
import hu.exercise.spring.kafka.event.ProductErrorEvent;
import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.ReadedFromDBEvent;
import hu.exercise.spring.kafka.event.Source;
import hu.exercise.spring.kafka.event.ValidProductEvent;
import hu.exercise.spring.kafka.input.Availability;
import hu.exercise.spring.kafka.input.MonetaryAmount;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.input.Run;

@TestPropertySource("/application.yml")
@SpringBootTest(classes = { KafkaTestConfig.class, KafkaSerdeConfig.class, KafkaStreamsConfig.class,
		KafkaEnvironment.class, JAXBConfig.class, ProductServiceSpy.class, PlatformTransactionManagerSpy.class,
		AppContextRefreshedEventPropertiesPrinter.class, InputOutputConfig.class })
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = "spring.kafka.bootstrap-servers", brokerProperties = {
		"log.dir=target/kafka-log", "auto.create.topics.enable=${kafka.broker.topics-enable:true}" })
public class KafkaStreamComplexTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamComplexTest.class);

	@Autowired
	StreamsBuilderFactoryBean factory;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	@Autowired
	KafkaStreamsConfig streamsConfig;

	@Autowired
	KafkaSerdeConfig kafkaSerdeConfig;

	@Autowired
	KafkaEnvironment environment;

	@Autowired
	ProductServiceSpy productServiceSpy;

	@Autowired
	PlatformTransactionManagerSpy platformTransactionManagerSpy;

	private EasyRandom generator = new EasyRandom();

	@BeforeEach
	public void beforeEach() {
		LOGGER.warn("beforeEach");

		this.embeddedKafka.addTopics(
				
				ReadedFromDBEvent.class.getName(), 
				ValidProductEvent.class.getName(), 
				ProductErrorEvent.class.getName(), 
				Flushed.class.getName(), 
				ProductEvent.class.getName(), 
				DBEvent.class.getName(), 
				Run.class.getName()
				
				);
				
		productServiceSpy.reset();
		platformTransactionManagerSpy.reset();

		// new requestid
		environment.init();
		environment.getRun().setFilename("unit-test.txt");
		environment.getReport().setSumReaded(0);

	}

	@AfterEach
	public void afterEach() throws Exception {
		LOGGER.warn("afterEach");
	}

	@DirtiesContext
	@Test
	public void test_RollupStream_complex() throws InterruptedException, ExecutionException {

		Consumer<String, Flushed> flushConsumer = flushConsumer();
		environment.getReport().setSumReaded(8);

		sendProductEvent(generateProductEvent("1", Source.DB));
		sendProductEvent(generateProductEvent("2", Source.DB));
		sendProductEvent(generateProductEvent("3", Source.DB));
		sendProductEvent(generateProductEvent("4", Source.DB));
		sendProductEvent(generateProductEvent("5", Source.DB));

		sendProductEvent(generateProductEvent("2", Source.TSV));
		sendProductEvent(generateProductEvent("3", Source.TSV));
		sendProductEvent(generateProductEvent("6", Source.TSV));

		initAndStartStream();

		this.embeddedKafka.consumeFromAnEmbeddedTopic(flushConsumer, Flushed.class.getName());

		ConsumerRecords<String, Flushed> flushRecords = KafkaTestUtils.getRecords(flushConsumer);
		AtomicInteger countInsert = new AtomicInteger();
		AtomicInteger countDelete = new AtomicInteger();
		AtomicInteger countUpdate = new AtomicInteger();
		List<Flushed> flushValues = new ArrayList<>();
		flushRecords.forEach(record -> {
			Flushed f = record.value();

			if (!environment.getRequestid().toString().equals(f.getRequestid())) {
				return;
			}

			countInsert.addAndGet(f.getCountInsert());
			countDelete.addAndGet(f.getCountDelete());
			countUpdate.addAndGet(f.getCountUpdate());
			flushValues.add(f);
		});

		LOGGER.warn("flushValues: " + flushValues);

		Assertions.assertNotEquals(0, flushValues.size());

		Flushed flushed = flushValues.get(flushValues.size() - 1);
		Assertions.assertEquals(environment.getReport().getSumReaded(), flushed.getSumProcessed());

		Assertions.assertEquals(1, flushed.getCountInsert());
		Assertions.assertEquals(3, flushed.getCountDelete());
		Assertions.assertEquals(2, flushed.getCountUpdate());

		Assertions.assertEquals(countInsert.intValue(), productServiceSpy.getCountInsert());
		Assertions.assertEquals(countDelete.intValue(), productServiceSpy.getCountDelete());
		Assertions.assertEquals(countUpdate.intValue(), productServiceSpy.getCountUpdate());

		Assertions.assertEquals(1, platformTransactionManagerSpy.getCommitCounter());
	}

	@DirtiesContext
	@Test
	public void test_RollupStream_insert_only() throws InterruptedException, ExecutionException {

		environment.getReport().setSumReaded(2);

		sendProductEvent(generateProductEvent("1", Source.TSV));
		sendProductEvent(generateProductEvent("2", Source.TSV));

		initAndStartStream();

		Consumer<String, Flushed> flushConsumer = flushConsumer();
		this.embeddedKafka.consumeFromAnEmbeddedTopic(flushConsumer, Flushed.class.getName());

		ConsumerRecords<String, Flushed> flushRecords = KafkaTestUtils.getRecords(flushConsumer);
		AtomicInteger countInsert = new AtomicInteger();
		AtomicInteger countDelete = new AtomicInteger();
		AtomicInteger countUpdate = new AtomicInteger();
		List<Flushed> flushValues = new ArrayList<>();
		flushRecords.forEach(record -> {
			Flushed f = record.value();
			if (!environment.getRequestid().toString().equals(f.getRequestid())) {
				LOGGER.warn("ignoring " + f.toString());
				return;
			}

			countInsert.addAndGet(f.getCountInsert());
			countDelete.addAndGet(f.getCountDelete());
			countUpdate.addAndGet(f.getCountUpdate());
			flushValues.add(f);
		});

		LOGGER.warn("flushValues: " + flushValues);

		Assertions.assertNotEquals(0, flushValues.size());

		Flushed flushed = flushValues.get(flushValues.size() - 1);
		Assertions.assertEquals(environment.getReport().getSumReaded(), flushed.getSumProcessed());

		Assertions.assertEquals(2, flushed.getCountInsert());
		Assertions.assertEquals(0, flushed.getCountDelete());
		Assertions.assertEquals(0, flushed.getCountUpdate());

		Assertions.assertEquals(countInsert.intValue(), productServiceSpy.getCountInsert());
		Assertions.assertEquals(countDelete.intValue(), productServiceSpy.getCountDelete());
		Assertions.assertEquals(countUpdate.intValue(), productServiceSpy.getCountUpdate());

		Assertions.assertEquals(1, platformTransactionManagerSpy.getCommitCounter());
	}

	@DirtiesContext
	@Test
	public void test_RollupStream_delete_only() throws InterruptedException, ExecutionException {

		environment.getReport().setSumReaded(3);

		sendProductEvent(generateProductEvent("1", Source.DB));
		sendProductEvent(generateProductEvent("2", Source.DB));
		sendProductEvent(generateProductEvent("3", Source.DB));

		initAndStartStream();

		Consumer<String, Flushed> flushConsumer = flushConsumer();
		this.embeddedKafka.consumeFromAnEmbeddedTopic(flushConsumer, Flushed.class.getName());

		ConsumerRecords<String, Flushed> flushRecords = KafkaTestUtils.getRecords(flushConsumer);
		AtomicInteger countInsert = new AtomicInteger();
		AtomicInteger countDelete = new AtomicInteger();
		AtomicInteger countUpdate = new AtomicInteger();
		List<Flushed> flushValues = new ArrayList<>();
		flushRecords.forEach(record -> {
			Flushed f = record.value();
			if (!environment.getRequestid().toString().equals(f.getRequestid())) {
				LOGGER.warn("ignoring " + f.toString());
				return;
			}

			countInsert.addAndGet(f.getCountInsert());
			countDelete.addAndGet(f.getCountDelete());
			countUpdate.addAndGet(f.getCountUpdate());
			flushValues.add(f);
		});

		LOGGER.warn("flushValues: " + flushValues);

		Assertions.assertNotEquals(0, flushValues.size());

		Flushed flushed = flushValues.get(flushValues.size() - 1);
		Assertions.assertEquals(environment.getReport().getSumReaded(), flushed.getSumProcessed());

		Assertions.assertEquals(0, flushed.getCountInsert());
		Assertions.assertEquals(3, flushed.getCountDelete());
		Assertions.assertEquals(0, flushed.getCountUpdate());

		Assertions.assertEquals(countInsert.intValue(), productServiceSpy.getCountInsert());
		Assertions.assertEquals(countDelete.intValue(), productServiceSpy.getCountDelete());
		Assertions.assertEquals(countUpdate.intValue(), productServiceSpy.getCountUpdate());

		Assertions.assertEquals(1, platformTransactionManagerSpy.getCommitCounter());
	}

	@DirtiesContext
	@Test
	public void test_RollupStream_update_only() throws InterruptedException, ExecutionException {

		environment.getReport().setSumReaded(4);

		sendProductEvent(generateProductEvent("1", Source.DB));
		sendProductEvent(generateProductEvent("2", Source.TSV));
		sendProductEvent(generateProductEvent("1", Source.TSV));
		sendProductEvent(generateProductEvent("2", Source.DB));

		initAndStartStream();

		Consumer<String, Flushed> flushConsumer = flushConsumer();
		this.embeddedKafka.consumeFromAnEmbeddedTopic(flushConsumer, Flushed.class.getName());

		ConsumerRecords<String, Flushed> flushRecords = KafkaTestUtils.getRecords(flushConsumer);
		AtomicInteger countInsert = new AtomicInteger();
		AtomicInteger countDelete = new AtomicInteger();
		AtomicInteger countUpdate = new AtomicInteger();
		List<Flushed> flushValues = new ArrayList<>();
		flushRecords.forEach(record -> {
			Flushed f = record.value();
			if (!environment.getRequestid().toString().equals(f.getRequestid())) {
				LOGGER.warn("ignoring " + f.toString());
				return;
			}

			countInsert.addAndGet(f.getCountInsert());
			countDelete.addAndGet(f.getCountDelete());
			countUpdate.addAndGet(f.getCountUpdate());
			flushValues.add(f);
		});

		LOGGER.warn("flushValues: " + flushValues);

		Assertions.assertNotEquals(0, flushValues.size());

		Flushed flushed = flushValues.get(flushValues.size() - 1);
		Assertions.assertEquals(environment.getReport().getSumReaded(), flushed.getSumProcessed());

		Assertions.assertEquals(0, flushed.getCountInsert());
		Assertions.assertEquals(0, flushed.getCountDelete());
		Assertions.assertEquals(2, flushed.getCountUpdate());

		Assertions.assertEquals(countInsert.intValue(), productServiceSpy.getCountInsert());
		Assertions.assertEquals(countDelete.intValue(), productServiceSpy.getCountDelete());
		Assertions.assertEquals(countUpdate.intValue(), productServiceSpy.getCountUpdate());

		Assertions.assertEquals(1, platformTransactionManagerSpy.getCommitCounter());
	}

	private void sendProductEvent(ProductEvent productEvent) throws InterruptedException, ExecutionException {
		LOGGER.warn("sending " + productEvent);
		this.kafkaTemplate()
				.send(ProductEvent.class.getName(), environment.getRequestid().toString(), productEvent)
				.get();
	}

	private void initAndStartStream() {

		factory.setCleanupConfig(new CleanupConfig(true, false));
		factory.setStreamsUncaughtExceptionHandler(ex -> {
			LOGGER.error("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread", ex);
			Assertions.fail(ex);
			return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
		});

		factory.start();

		while (!factory.isRunning()) {
		}
	}

	private ProductEvent generateProductEvent(String id, Source source) {
		ProductEvent productEvent = new ProductEvent();
		productEvent.setSource(source);
		productEvent.setId(id);
		productEvent.setRequestid(environment.getRequestid().toString());

		Product product = new Product();
		product.setId(id);
		product.setTitle(generator.nextObject(String.class));
		product.setLink("http://test");
		MonetaryAmount price = new MonetaryAmount();
		price.setAmount(BigDecimal.valueOf(generator.nextDouble(20_000)));
		price.setCurrency(Currency.getInstance("EUR"));
		product.setPrice(price);
		product.setDescription(generator.nextObject(String.class));
		product.setImage_link("http://image_link_test");
		product.setAvailability(Availability.IN_STOCK);
		productEvent.setProduct(product);
		return productEvent;
	}

	public DefaultKafkaConsumerFactory<String, Flushed> consumerFactory() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false",
				this.embeddedKafka);
		consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10000);
		DefaultKafkaConsumerFactory<String, Flushed> kafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
				consumerProps, Serdes.String().deserializer(), kafkaSerdeConfig.flushedSerde().deserializer());
		return kafkaConsumerFactory;
	}

	public ProducerFactory<String, ProductEvent> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	public Map<String, Object> producerConfigs() {
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return producerProps;
	}

	public KafkaTemplate<String, ProductEvent> kafkaTemplate() {
		KafkaTemplate<String, ProductEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory());
		return kafkaTemplate;
	}

	public Consumer<String, Flushed> flushConsumer() {
		return consumerFactory().createConsumer();
	}

}
