package hu.exercise.spring.kafka.service;

import java.util.stream.Stream;

import hu.exercise.spring.kafka.input.Product;

public interface ProductService {

	public Stream<Product> getAllProducts(String requestid);

	public Iterable<Product> bulkInsertProducts(Iterable<Product> productList);

	public Iterable<Product> bulkUpdateProducts(Iterable<Product> productList);
	
	public void bulkDeleteProducts(Iterable<Product> productList);

}
