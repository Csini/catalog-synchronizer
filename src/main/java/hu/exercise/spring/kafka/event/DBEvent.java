package hu.exercise.spring.kafka.event;

import hu.exercise.spring.kafka.cogroup.Action;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DBEvent {

	@Schema(description = "Run's unique identifier", example = "a3dbaa5a-1375-491e-8c21-403864de8779")
	private String requestid;
	
	@Schema(description = "Your product’s unique identifier", example = "A2B4")
	private String id;
	
	private Action action;
}
