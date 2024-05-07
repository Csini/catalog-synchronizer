package hu.exercise.spring.kafka.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.input.Run;
import hu.exercise.spring.kafka.repository.RunRepository;
import hu.exercise.spring.kafka.service.RunService;

@Service
public class RunServiceImpl implements RunService {

	private RunRepository repository;

	public RunServiceImpl(@Autowired RunRepository repository) {
		super();
		this.repository = repository;
	}

	public void setRepository(RunRepository repository) {
		this.repository = repository;
	}

	@Override
	public Run saveRun(Run run) {
		return repository.save(run);
	}

}
