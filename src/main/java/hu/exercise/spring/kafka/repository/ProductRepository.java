package hu.exercise.spring.kafka.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hu.exercise.spring.kafka.input.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, String> {

//	@Query(value = "SELECT p FROM Product p", nativeQuery = false)
//	Stream<Product> findAllProductsNative(String requestid);
}
