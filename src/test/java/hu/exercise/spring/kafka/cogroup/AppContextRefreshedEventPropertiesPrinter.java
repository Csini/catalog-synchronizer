package hu.exercise.spring.kafka.cogroup;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

@Component
public class AppContextRefreshedEventPropertiesPrinter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppContextRefreshedEventPropertiesPrinter.class);

	@EventListener
	public void handleContextRefreshed(ContextRefreshedEvent event) {
		printAllActiveProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());

		printAllApplicationProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
	}

	private void printAllActiveProperties(ConfigurableEnvironment env) {

		LOGGER.warn("************************* ALL PROPERTIES(EVENT) ******************************");

		env.getPropertySources().stream().filter(ps -> ps instanceof MapPropertySource)
				.map(ps -> ((MapPropertySource) ps).getSource().keySet()).flatMap(Collection::stream).distinct()
				.sorted().forEach(key -> LOGGER.warn("{}={}", key, env.getProperty(key)));

		LOGGER.warn("******************************************************************************");
	}

	private void printAllApplicationProperties(ConfigurableEnvironment env) {

		LOGGER.warn("************************* APP PROPERTIES(EVENT) ******************************");

		env.getPropertySources().stream()
				.filter(ps -> ps instanceof MapPropertySource && ps.getName().contains("application.properties"))
				.map(ps -> ((MapPropertySource) ps).getSource().keySet()).flatMap(Collection::stream).distinct()
				.sorted().forEach(key -> LOGGER.warn("{}={}", key, env.getProperty(key)));

		LOGGER.warn("******************************************************************************");
	}
}