package hu.exercise.spring.kafka.service;

import java.util.List;

import hu.exercise.spring.kafka.input.Product;
import jakarta.persistence.EntityNotFoundException;

public interface ProductService {

	public List<Product> getAllProducts();

	public Product getProduct(String id) throws EntityNotFoundException;

	public String deleteProduct(String id) throws EntityNotFoundException;

	public Product saveProduct(Product Product);

	public long getCountAllProducts();

}
