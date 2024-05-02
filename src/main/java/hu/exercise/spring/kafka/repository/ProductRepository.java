package hu.exercise.spring.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.exercise.spring.kafka.input.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

}
