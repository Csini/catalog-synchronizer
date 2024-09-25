package hu.exercise.spring.kafka.service.impl;

import java.util.Collection;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.KafkaUtils;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.event.DBEvent;
import hu.exercise.spring.kafka.event.DBEventMessageProducer;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.repository.ProductRepository;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	private ProductRepository repository;

	private DBEventMessageProducer dbEventMessageProducer;

	private KafkaEnvironment environment;

	public ProductServiceImpl(@Autowired KafkaEnvironment environment, @Autowired ProductRepository repository,
			@Autowired DBEventMessageProducer dbEventMessageProducer) {
		super();
		this.environment = environment;
		this.repository = repository;
		this.dbEventMessageProducer = dbEventMessageProducer;
	}

	public void setRepository(ProductRepository repository) {
		this.repository = repository;
	}

	@Override
	public Stream<Product> getAllProducts(String requestid) {
		Stream<Product> allByOrderByIdAsc = KafkaUtils.getStreamFromIterator(repository.findAll().iterator());
		LOGGER.info("allByOrderByIdAsc");
		return allByOrderByIdAsc;
	}

	@Override
	public Iterable<Product> bulkInsertProducts(Collection<Product> productList) {

		Iterable<Product> saveAll = repository.saveAll(productList);
		
		dbEventMessageProducer.sendAll(saveAll, Action.INSERT);

//		saveAll.forEach(p -> dbEventMessageProducer
//				.sendMessage(new DBEvent(environment.getRequestid().toString(), p.getId(), Action.INSERT)));
		environment.getReport().setSumProcessed(environment.getReport().getSumProcessed() + productList.size());
		environment.getReport().printProgressbar();
		return saveAll;
	}

	@Override
	public Iterable<Product> bulkUpdateProducts(Collection<Product> productList) {
		Iterable<Product> saveAll = repository.saveAll(productList);
		
		dbEventMessageProducer.sendAll(saveAll, Action.UPDATE);
		
//		saveAll.forEach(p -> dbEventMessageProducer
//				.sendMessage(new DBEvent(environment.getRequestid().toString(), p.getId(), Action.UPDATE)));
		environment.getReport().setSumProcessed(environment.getReport().getSumProcessed() + productList.size());
		environment.getReport().printProgressbar();
		return saveAll;
	}

	@Override
	public void bulkDeleteProducts(Collection<Product> productList) {
		repository.deleteAll(productList);
		
		dbEventMessageProducer.sendAll(productList, Action.DELETE);
		
//		productList.forEach(p -> dbEventMessageProducer
//				.sendMessage(new DBEvent(environment.getRequestid().toString(), p.getId(), Action.DELETE)));
		environment.getReport().setSumProcessed(environment.getReport().getSumProcessed() + productList.size());
		environment.getReport().printProgressbar();
	}

}
