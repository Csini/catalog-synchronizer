package hu.exercise.spring.kafka.event;

import hu.exercise.spring.kafka.cogroup.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DBEvent {

	private String requestid;
	
	private String id;
	
	private Action action;
}
