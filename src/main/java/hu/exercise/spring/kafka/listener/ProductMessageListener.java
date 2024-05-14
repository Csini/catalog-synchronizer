package hu.exercise.spring.kafka.listener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import hu.exercise.spring.kafka.KafkaEnvironment;
import hu.exercise.spring.kafka.cogroup.Action;
import hu.exercise.spring.kafka.cogroup.ProductRollup;
import hu.exercise.spring.kafka.input.Product;
import hu.exercise.spring.kafka.service.ProductService;

@Service
public class ProductMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMessageListener.class);

//	@Autowired
//	public NewTopic mergedProductEvents;

//	private CountDownLatch productPairLatch = new CountDownLatch(1);

	@Autowired
	private ProductService productService;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	public NewTopic productRollup;

	private int count = 0;

	@KafkaListener(topics = "#{__listener.topic}", containerFactory = "productPairKafkaListenerContainerFactory", batch = "true")
	public void productPairListener(List<ProductRollup> productRollupList) {
//		LOGGER.info("Received ProductPair message: " + productPair.getId() + " " + productPair.getPair().getAction());
//		this.productPairLatch.countDown();

//		if(Math.random()>0.9) {
//			productService.saveProduct(productPair.getPair().getReadedFromFile());
//		}
		int size = productRollupList.size();
		LOGGER.warn("Received Product List<ProductRollup> message count: " + size + " sum: " + (count += size));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("" + productRollupList);
		}

		Map<Action, List<Product>> groupedProductRollups = productRollupList.stream().map(rollup -> {
			Product productToSave = rollup.getPair().getProductToSave();
			Action action = rollup.getPair().getAction();
//			LOGGER.warn("" + productToSave.getId() + ": " + action);
			return rollup;

		}).collect(Collectors.groupingBy(a -> a.getPair().getAction(), Collectors.mapping(rollup -> {
			Product productToSave = rollup.getPair().getProductToSave();
			return productToSave;
		}, Collectors.toList())));

		groupedProductRollups.entrySet().forEach(entry -> {
			LOGGER.warn(entry.getKey() + ": " + entry.getValue().size());
		});

		if (groupedProductRollups.containsKey(Action.DELETE)) {
//			groupedProductRollups.get(Action.DELETE).forEach(p -> productService.deleteProduct(p.getId()));
			productService.bulkDeleteProducts(groupedProductRollups.get(Action.DELETE));
		}
		if (groupedProductRollups.containsKey(Action.UPDATE)) {
			productService.bulkSaveProducts(groupedProductRollups.get(Action.UPDATE));
		}
		if (groupedProductRollups.containsKey(Action.INSERT)) {
			productService.bulkSaveProducts(groupedProductRollups.get(Action.INSERT));
		}

		if (groupedProductRollups.containsKey(Action.ERROR)) {
			LOGGER.error("ERROR: " + groupedProductRollups.get(Action.ERROR));
		}
	}

	public String getTopic() {
		return productRollup.name();
	}

}