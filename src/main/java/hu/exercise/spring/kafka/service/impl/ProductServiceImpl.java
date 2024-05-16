package hu.exercise.spring.kafka.service.impl;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaUtils;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.repository.ProductRepository;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	private ProductRepository repository;

	public ProductServiceImpl(@Autowired ProductRepository repository) {
		super();
		this.repository = repository;
	}

	public void setRepository(ProductRepository repository) {
		this.repository = repository;
	}

	@Override
	public Stream<Product> getAllProducts(String requestid) {
		Stream<Product> allByOrderByIdAsc = KafkaUtils.getStreamFromIterator(repository.findAll().iterator());
		LOGGER.warn("allByOrderByIdAsc");
		return allByOrderByIdAsc;
	}

	@Override
	public Iterable<Product> bulkSaveProducts(Iterable<Product> productList) {
		return repository.saveAll(productList);
	}

	@Override
	public void bulkDeleteProducts(Iterable<Product> productList) {
		repository.deleteAll(productList);
	}

}
