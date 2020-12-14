package cxn.pub.pgrls.configuration;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring will generate these database properties automagically
 * from application.properties which is populated via environment
 * variables. We have 2 different sets of properties. One for the
 * application to connect as -- and be constrained by RLS policies.
 * The 2nd is to connect as the database owner which will circumvent
 * RLS to allow for administrative commands.
 */
@Configuration
public class DataSourcePropertiesConfiguration {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "admin.datasource")
	public DataSourceProperties adminDataSourceProperties() {
		return new DataSourceProperties();
	}
}
