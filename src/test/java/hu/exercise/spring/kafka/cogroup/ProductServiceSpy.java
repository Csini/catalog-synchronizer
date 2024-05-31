package hu.exercise.spring.kafka.cogroup;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;
import lombok.Data;

@Service
@Data
public class ProductServiceSpy implements ProductService {

	private int countInsert;
	private int countUpdate;
	private int countDelete;

	public void reset() {
		countInsert = 0;
		countUpdate = 0;
		countDelete = 0;
	}

	@Override
	public Stream<Product> getAllProducts(String requestid) {
		return Stream.of();
	}

	@Override
	public Iterable<Product> bulkInsertProducts(Iterable<Product> productList, int productListSize) {
		countInsert += productListSize;
		return productList;
	}

	@Override
	public Iterable<Product> bulkUpdateProducts(Iterable<Product> productList, int productListSize) {
		countUpdate += productListSize;
		return productList;
	}

	@Override
	public void bulkDeleteProducts(Iterable<Product> productList, int productListSize) {
		countDelete += productListSize;
	}

}
