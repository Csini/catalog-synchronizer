package hu.exercise.spring.kafka.cogroup;

import java.util.UUID;

import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;

public class ProductAggregator implements Aggregator<String, ProductEvent, ProductRollup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductAggregator.class);

	private UUID requestid;

	public ProductAggregator(UUID requestid) {
		super();
		this.requestid = requestid;
	}

	@Override
	public ProductRollup apply(final String rollupid, final ProductEvent productEvent,
			final ProductRollup productRollup) {

//		if(!requestid.equals(""+productEvent.getRequestid())){
//			LOGGER.error("uuuiiidd: "+productEvent);
//			return productRollup;
//		}
//		
		if (productRollup.getProductrollupid() != null && !productRollup.getProductrollupid().startsWith(rollupid)) {
			LOGGER.error("uuuiiidd: pr " + productRollup);
			return productRollup;
		}
		productRollup.setProductrollupid(rollupid);

		ProductPair pair = productRollup.getPair();

//		if (pair == null) {
//			pair = new ProductPair();
//			productRollup.getPair().put(productEvent.getId(), pair);
//		}

		final Source source = productEvent.getSource();

		if (Source.DB.equals(source)) {
			pair.setReadedFromDb(productEvent);
		} else if (Source.TSV.equals(source)) {
			pair.setReadedFromFile(productEvent);
		}

//		LOGGER.debug("productRollup: " + productRollup);
		return productRollup;
	}
}
