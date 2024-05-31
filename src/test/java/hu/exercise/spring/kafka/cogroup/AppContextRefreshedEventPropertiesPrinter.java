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
		if (LOGGER.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("\n");
			sb.append("************************* ALL PROPERTIES(EVENT) ******************************");

			sb.append("\n");

			env.getPropertySources().stream().filter(ps -> ps instanceof MapPropertySource)
					.map(ps -> ((MapPropertySource) ps).getSource().keySet()).flatMap(Collection::stream).distinct()
					.sorted().forEach(key -> {
						sb.append(key + "=" + env.getProperty(key));
						sb.append("\n");
					});

			sb.append("******************************************************************************");
			LOGGER.debug(sb.toString());
		}
	}

	private void printAllApplicationProperties(ConfigurableEnvironment env) {

		if (LOGGER.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("\n");
			sb.append("************************* APP PROPERTIES(EVENT) ******************************");
			sb.append("\n");
			env.getPropertySources().stream()
					.filter(ps -> ps instanceof MapPropertySource && ps.getName().contains("application.properties"))
					.map(ps -> ((MapPropertySource) ps).getSource().keySet()).flatMap(Collection::stream).distinct()
					.sorted().forEach(key -> {
						sb.append(key + "=" + env.getProperty(key));
						sb.append("\n");
					});

			sb.append("******************************************************************************");
			LOGGER.debug(sb.toString());
		}
	}
}