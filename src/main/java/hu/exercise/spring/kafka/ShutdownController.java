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
import org.springframework.transaction.PlatformTransactionManager;

import hu.exercise.spring.kafka.cogroup.Report;

@Controller
public class ShutdownController implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownController.class);

	private ApplicationContext context;

	@Autowired
	public KafkaEnvironment environment;

	@Autowired
	private KafkaReportController reportController;

	@Autowired
	private PlatformTransactionManager txManager;
	
	public void shutdownContext() {

		LOGGER.warn("Shutting down Context...");
//		TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
//		if (transactionStatus != null && !transactionStatus.isCompleted()) {
//			txManager.commit(transactionStatus);
//		}
		((ConfigurableApplicationContext) context).close();
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.context = ctx;

	}

	public void shutdownContextWithError(int errorCode, Throwable e) {
		LOGGER.error("" + errorCode, e);
		LOGGER.warn("Exiting with error " + errorCode + "...");
//		try {
//			reportController.createErrorReport(errorCode, e);
//		} catch (JAXBException | IOException | URISyntaxException ex) {
//			// we can't do anything
//			LOGGER.error("createErrorReport error", ex);
//		}
		Report report = environment.getReport();
		report.setReportedThrowable(e);
		report.setErrorCode(errorCode);
//		TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
//		if (transactionStatus != null && !transactionStatus.isCompleted()) {
//			txManager.rollback(transactionStatus);
//		}
		System.exit(SpringApplication.exit(this.context, () -> errorCode));
	}
}