package hu.exercise.spring.kafka.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.repository.ProductRepository;
import hu.exercise.spring.kafka.service.ProductService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductServiceImpl implements ProductService {

	private ProductRepository repository;

	public ProductServiceImpl(@Autowired ProductRepository repository) {
		super();
		this.repository = repository;
	}

	public void setRepository(ProductRepository repository) {
		this.repository = repository;
	}

	@Override
	public Iterable<Product> getAllProducts() {
		return repository.findAll();
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
	
	@Override
	public void bulkDeleteProducts(Iterable<Product> productList) {
		repository.deleteAll(productList);
	}

	@Override
	public long getCountAllProducts() {
		return repository.count();
	}

}
