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
	public List<Product> getAllProducts() {
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
	public Product saveProduct(Product Product) {
		return repository.saveAndFlush(Product);
	}

	@Override
	public long getCountAllProducts() {
		return repository.count();
	}

}
