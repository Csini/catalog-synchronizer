package hu.exercise.spring.kafka.cogroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "requestid")
public class Flushed {

	private String requestid;

	private int countInsert;
	
	private int countUpdate;
	
	private int countDelete;
	
	private int countError;
	
	private int sumProcessed;
}
