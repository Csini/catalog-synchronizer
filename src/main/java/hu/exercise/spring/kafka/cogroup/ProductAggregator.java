package hu.exercise.spring.kafka.cogroup;

import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.exercise.spring.kafka.event.ProductEvent;
import hu.exercise.spring.kafka.event.Source;

public class ProductAggregator implements Aggregator<String, ProductEvent, ProductRollup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductAggregator.class);
	
	@Override
	public ProductRollup apply(final String id, final ProductEvent productEvent, final ProductRollup productRollup) {

//		ProductPair pair = productRollup.getProducts().get(id);
//		if (pair == null) {
//			pair = new ProductPair();
//			productRollup.getProducts().put(id, pair);
//		}
		ProductPair pair = productRollup.getPair();
		
		productRollup.setId(id);
	
		final Source source = productEvent.getSource();

		if (Source.DB.equals(source)) {
			pair.setReadedFromDb(productEvent.getProduct());
		} else if (Source.TSV.equals(source)) {
			pair.setReadedFromFile(productEvent.getProduct());
		}
		
		LOGGER.debug("productRollup: " + productRollup);
		return productRollup;
	}
}
