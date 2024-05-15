package hu.exercise.spring.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class ShutdownController implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownController.class);

	private ApplicationContext context;

	@Autowired
	public KafkaEnvironment environment;

	public void shutdownContext() {

		LOGGER.warn("Shutting down Context...");

		// TODO
//    	environment.setRequestid(null);
//    	environment.setFilenane(null);

		((ConfigurableApplicationContext) context).close();
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.context = ctx;

	}

	public void shutdownContextWithError(int errorCode) {
		LOGGER.warn("Exiting with error " + errorCode + "...");
		SpringApplication.exit(this.context, () -> errorCode);
	}
}