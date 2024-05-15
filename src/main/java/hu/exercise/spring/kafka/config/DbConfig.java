package hu.exercise.spring.kafka.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "hu.exercise.spring.kafka.repository")
@PropertySource("persistence.properties")
@EnableTransactionManagement
public class DbConfig {

	@Autowired
	private Environment env;

	@Bean
	public DataSource dataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		final SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
		dataSource.setDriverClassName(env.getProperty("driverClassName"));
		dataSource.setUrl(env.getProperty("url"));
		dataSource.setUsername(env.getProperty("user"));
		dataSource.setPassword(env.getProperty("password"));

//		dataSource.setSuppressClose(true);
		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan(new String[] { "hu.exercise.spring.kafka.input" });
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		em.setJpaProperties(additionalProperties());
		return em;
	}

	final Properties additionalProperties() {
		final Properties hibernateProperties = new Properties();
		if (env.getProperty("hibernate.hbm2ddl.auto") != null) {
			hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
		}
		if (env.getProperty("hibernate.dialect") != null) {
			hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
		}
		if (env.getProperty("hibernate.show_sql") != null) {
			hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
		}

		if (env.getProperty("hibernate.generate_statistics") != null) {
			hibernateProperties.setProperty("hibernate.generate_statistics",
					env.getProperty("hibernate.generate_statistics"));
		}

		if (env.getProperty("hibernate.jdbc.batch_size") != null) {
			hibernateProperties.setProperty("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size"));
		}

		if (env.getProperty("hibernate.jdbc.batch_versioned_data") != null) {
			hibernateProperties.setProperty("hibernate.jdbc.batch_versioned_data",
					env.getProperty("hibernate.jdbc.batch_versioned_data"));
		}

		return hibernateProperties;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}

}