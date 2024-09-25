package hu.exercise.spring.kafka.topic;

import hu.exercise.spring.kafka.cogroup.Action;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.csini.spring.kafka.KafkaEntity;
import net.csini.spring.kafka.KafkaEntityKey;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@KafkaEntity
public class DBEvent {

	@KafkaEntityKey
	@Schema(description = "Run's unique identifier", example = "a3dbaa5a-1375-491e-8c21-403864de8779")
	private String requestid;
	
	@Schema(description = "Your product’s unique identifier", example = "A2B4")
	private String id;
	
	private Action action;
}
