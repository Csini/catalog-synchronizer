package hu.exercise.spring.kafka.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hu.exercise.spring.kafka.input.Run;

@Repository
public interface RunRepository extends CrudRepository<Run, UUID> {

}
