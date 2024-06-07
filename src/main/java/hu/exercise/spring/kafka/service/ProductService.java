package hu.exercise.spring.kafka.service;

import java.util.Collection;
import java.util.stream.Stream;

import hu.exercise.spring.kafka.input.Product;

public interface ProductService {

	public Stream<Product> getAllProducts(String requestid);

	public Iterable<Product> bulkInsertProducts(Collection<Product> productList);

	public Iterable<Product> bulkUpdateProducts(Collection<Product> productList);
	
	public void bulkDeleteProducts(Collection<Product> productList);

}
