package hu.exercise.spring.kafka.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.cogroup.CustomProductPairAggregator;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.repository.CustomProductRepository;
import hu.exercise.spring.kafka.repository.ProductRepository;
import hu.exercise.spring.kafka.service.ProductService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	private ProductRepository repository;

	private CustomProductRepository customRepository;

//	private Map<String, Product> readedFromDbMap = new HashMap<String, Product>();

	public ProductServiceImpl(@Autowired ProductRepository repository,
			@Autowired CustomProductRepository customRepository) {
		super();
		this.repository = repository;
		this.customRepository = customRepository;
	}

	public void setRepository(ProductRepository repository) {
		this.repository = repository;
	}

	public void setCustomRepository(CustomProductRepository customRepository) {
		this.customRepository = customRepository;
	}

	@Override
	public Stream<Product> getAllProducts(String requestid) {
//		return customRepository.findAllProductsNative(requestid);
//		Stream<Product> allByOrderByIdAsc = repository.findByOrderByIdAsc();
		Stream<Product> allByOrderByIdAsc = CustomProductPairAggregator.getStreamFromIterator(repository.findAll().iterator());
		LOGGER.warn("allByOrderByIdAsc");
		return allByOrderByIdAsc;
//				.map(p -> {
//			readedFromDbMap.put(p.getId(), p);
//			return p;
//		});
	}

	@Override
	public Product getProduct(String id) throws EntityNotFoundException {
		return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product " + id + " not found"));
	}

	@Override
	public String deleteProduct(String id) {
		repository.deleteById(id);
		return id;
	}

	@Override
	public Product saveProduct(Product product) {
		return repository.save(product);
	}

	@Override
	public Iterable<Product> bulkSaveProducts(Iterable<Product> productList) {
		return repository.saveAll(productList);
	}

//	@Override
//	public void bulkInsertProducts(Iterable<Product> productList) {
//		customRepository.insertAll(productList);
//	}
//
//	@Override
//	public void bulkUpdateProducts(Iterable<Product> productList) {
//		customRepository.updateAll(productList);
//	}

	@Override
	public void bulkDeleteProducts(Iterable<Product> productList) {
//		customRepository.deleteAll(productList);
		repository.deleteAll(productList);
	}

	@Override
	public long getCountAllProducts() {
		return repository.count();
	}

//	@Override
//	public Map<String, Product> getReadedFromDbMap() {
//		return readedFromDbMap;
//	}

}
