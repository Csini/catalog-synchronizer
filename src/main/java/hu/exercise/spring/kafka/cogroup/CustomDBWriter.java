package hu.exercise.spring.kafka.cogroup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

public class CustomDBWriter implements Processor<String, ProductRollup, String, Flushed> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomDBWriter.class);

	private ProcessorContext<String, Flushed> context;

	private ProductService productService;

	private KafkaEnvironment environment;

	private int flushCounter = 0;

	private PlatformTransactionManager txManager;

	private TransactionStatus status;

	private Map<String, Product> readedFromDbMap = new HashMap<String, Product>();

	public CustomDBWriter(KafkaEnvironment environment, ProductService productService,
			PlatformTransactionManager txManager) {
		super();
		this.environment = environment;
		this.productService = productService;
		this.txManager = txManager;
	}

	@Override
	public void init(final ProcessorContext<String, Flushed> context) {
		this.context = context;
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		// explicitly setting the transaction name is something that can be done only
		// programmatically
		def.setName("SomeTxName");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

		this.status = txManager.getTransaction(def);
		this.productService.getAllProducts(environment.getRequestid().toString())
				.forEach(p -> readedFromDbMap.put(p.getId(), p));
	}

	@Override
	public void process(Record<String, ProductRollup> rec) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processing: " + rec.value().getFlushid());
		}

		flushCounter++;

		AtomicInteger counter = new AtomicInteger();

		Map<Action, List<Product>> groupedProductRollups = group(rec, counter);

		Flushed flushed = Flushed.builder().requestid(environment.getRequestid().toString())
				.sumProcessed(rec.value().getProcessed()).build();

		if (groupedProductRollups.containsKey(Action.DELETE)) {
			List<Product> productList = groupedProductRollups.get(Action.DELETE);
			productService.bulkDeleteProducts(productList);
			flushed.setCountDelete(productList.size());
		}
		if (groupedProductRollups.containsKey(Action.UPDATE)) {
			List<Product> productList = groupedProductRollups.get(Action.UPDATE);
			productService.bulkUpdateProducts(productList);
			flushed.setCountUpdate(productList.size());
		}
		if (groupedProductRollups.containsKey(Action.INSERT)) {
			List<Product> productList = groupedProductRollups.get(Action.INSERT);
			productService.bulkInsertProducts(productList);
			flushed.setCountInsert(productList.size());
		}

		if (groupedProductRollups.containsKey(Action.ERROR)) {
			List<Product> productList = groupedProductRollups.get(Action.ERROR);
			LOGGER.error("ERROR: " + productList);
			flushed.setCountError(productList.size());
		}

		LOGGER.info("processed: " + counter);

		environment.getReport().setSumDBEvents(environment.getReport().getSumDBEvents() + counter.intValue());

		if (environment.getReport().getSumEvent() <= flushed.getSumProcessed()) {
			LOGGER.info("commiting.. ");
			this.txManager.commit(this.status);
		}

		RecordHeaders headers = new RecordHeaders();
		RecordHeader keyHeader = new RecordHeader("__Key_TypeId__", "java.lang.String".getBytes());
		headers.add(keyHeader);
		RecordHeader valueHeader = new RecordHeader("__TypeId__", "hu.exercise.spring.kafka.cogroup.Flushed".getBytes());
		headers.add(valueHeader);
		context.forward(new Record<String, Flushed>("\"" + environment.getRequestid().toString() + "\"", flushed,
				new Date().getTime(), headers));
	}

	private Map<Action, List<Product>> group(Record<String, ProductRollup> rec, AtomicInteger counter) {
		Map<Action, List<Product>> groupedProductRollups = rec.value().getPairList().stream()
				.map((ProductPair pair) -> {
//					Product productToSave = pair.getProductToSave();
//					Action action = pair.getAction();
//					LOGGER.warn("" + productToSave.getId() + ": " + action);
					counter.incrementAndGet();
					return pair;

				}).collect(Collectors.groupingBy(a -> a.getAction(), Collectors.mapping(pair -> {
					Product productToSave = pair.getProductToSave();

					// merge with attached JPA Entity
					Product readedFromDb = readedFromDbMap.get(productToSave.getId());

					if (readedFromDb != null) {
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("merging: " + pair.getAction() + " " + pair.getId());
						}
						readedFromDbMap.remove(productToSave.getId());
						readedFromDb.merge(productToSave);

						if (Action.UPDATE.equals(pair.getAction()) && !readedFromDb.isChange()) {
							this.environment.getReport()
									.setCountNoChange(this.environment.getReport().getCountNoChange() + 1);
						}
						return readedFromDb;
					}

					productToSave.setNew(Action.INSERT.equals(pair.getAction()));

					return productToSave;
				}, Collectors.toList())));
		return groupedProductRollups;
	}

	@Override
	public void close() {
		LOGGER.info("flushCounter: " + flushCounter);
		flushCounter = 0;
	}

}