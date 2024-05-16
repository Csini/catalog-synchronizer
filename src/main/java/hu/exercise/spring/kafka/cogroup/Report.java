package hu.exercise.spring.kafka.cogroup;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Report {
	private String requestid;

	private int countInsert;

	private int countUpdate;

	private int countDelete;

	private int countError;

	private int countReadedFromDB;

	private long countReadedFromTsv;
	private int countReadedFromTsvValid;
	private int countReadedFromTsvInvalid;

	private int sumProcessed;
	
	private int sumEvent;
	
	private int errorCode;
	
	private Throwable reportedThrowable;

}
