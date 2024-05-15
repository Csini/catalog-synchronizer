package hu.exercise.spring.kafka.service;

import java.util.Map;
import java.util.stream.Stream;

import hu.exercise.spring.kafka.input.Product;
import jakarta.persistence.EntityNotFoundException;

public interface ProductService {

	public Stream<Product> getAllProducts(String requestid);

	public Product getProduct(String id) throws EntityNotFoundException;

	public String deleteProduct(String id) throws EntityNotFoundException;

	public Product saveProduct(Product product);
	
	public Iterable<Product> bulkSaveProducts(Iterable<Product> productList);

	public void bulkDeleteProducts(Iterable<Product> productList);

	public long getCountAllProducts();

//	public void bulkUpdateProducts(Iterable<Product> productList);

//	public void bulkInsertProducts(Iterable<Product> productList);
	
//	public Map<String, Product> getReadedFromDbMap();

}
