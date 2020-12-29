package cxn.pub.pgrls.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

/**
 * Generates JDBC data source pool using the table OWNER credentials.
 * This connection will not be bound by RLS policies in order to
 * perform INSERTs and other actions regardless of the current
 * tenant context.
 */
@Repository
@Configuration
public class AdminDataSourceRepository {

	private static final Logger logger = LoggerFactory.getLogger(AdminDataSourceRepository.class);

	// See DataSourcePropertiesConfiguration
	@Autowired
	@Qualifier("adminDataSourceProperties")
	DataSourceProperties adminDataSourceProperties;

	public DataSource dataSource() {
		logger.error(adminDataSourceProperties.determineUrl());
		logger.error(adminDataSourceProperties.determineUsername());
		logger.error(adminDataSourceProperties.determinePassword());
		logger.error(adminDataSourceProperties.determineDatabaseName());
		return adminDataSourceProperties.initializeDataSourceBuilder().build();
	}
}
