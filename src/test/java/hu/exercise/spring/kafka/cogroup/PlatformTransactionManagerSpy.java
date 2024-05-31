package hu.exercise.spring.kafka.cogroup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import lombok.Data;

@Service
@Data
public class PlatformTransactionManagerSpy implements PlatformTransactionManager {

	private int commitCounter;

	private int rollbackCounter;

	private int transactionCounter;

	public void reset() {
		commitCounter = 0;
		rollbackCounter = 0;
		transactionCounter = 0;
	}

	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {

		transactionCounter++;
		return null;
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		commitCounter++;
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
		rollbackCounter++;
	}

}
