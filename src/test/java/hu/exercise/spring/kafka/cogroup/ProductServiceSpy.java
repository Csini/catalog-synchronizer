package hu.exercise.spring.kafka.cogroup;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductServiceSpy implements ProductService {

	@Override
	public Stream<Product> getAllProducts(String requestid) {
		return Stream.of();
	}

	@Override
	public Iterable<Product> bulkInsertProducts(Iterable<Product> productList, int productListSize) {
		return new ArrayList<Product>();
	}

	@Override
	public Iterable<Product> bulkUpdateProducts(Iterable<Product> productList, int productListSize) {
		return new ArrayList<Product>();
	}

	@Override
	public void bulkDeleteProducts(Iterable<Product> productList, int productListSize) {
		
	}

}
