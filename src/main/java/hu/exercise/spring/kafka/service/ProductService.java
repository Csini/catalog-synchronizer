package hu.exercise.spring.kafka.service;

import java.util.List;

import hu.exercise.spring.kafka.input.Product;
import jakarta.persistence.EntityNotFoundException;

public interface ProductService {

	public Iterable<Product> getAllProducts();

	public Product getProduct(String id) throws EntityNotFoundException;

	public String deleteProduct(String id) throws EntityNotFoundException;

	public Product saveProduct(Product product);
	
	public Iterable<Product> bulkSaveProducts(Iterable<Product> productList);

	public void bulkDeleteProducts(Iterable<Product> productList);

	public long getCountAllProducts();

}
