package hu.exercise.spring.kafka.cogroup;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@NoArgsConstructor
@EqualsAndHashCode(of = { "requestid", "flushid" })
public class ProductRollup {

	private String requestid;

	private int flushid;

	private int processed;

	private List<ProductPair> pairList = new ArrayList<>();

	public ProductRollup(String requestid, int flushid, int processed) {
		super();
		this.requestid = requestid;
		this.flushid = flushid;
		this.processed = processed;
	}

}
