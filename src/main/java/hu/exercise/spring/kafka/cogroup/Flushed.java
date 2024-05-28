package hu.exercise.spring.kafka.cogroup;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@Schema(description = "Run's unique identifier", example = "a3dbaa5a-1375-491e-8c21-403864de8779")
	private String requestid;

	private int countInsert;
	
	private int countUpdate;
	
	private int countDelete;
	
	private int countError;
	
	private int sumProcessed;
}
