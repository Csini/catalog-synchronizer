package hu.exercise.spring.kafka;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class ShutdownController implements ApplicationContextAware {
    
    private ApplicationContext context;
    
	@Autowired
	public KafkaEnvironment environment;
    
    public void shutdownContext() {
    	
    	//TODO
//    	environment.setRequestid(null);
//    	environment.setFilenane(null);
    	
        ((ConfigurableApplicationContext) context).close();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
        
    }
}