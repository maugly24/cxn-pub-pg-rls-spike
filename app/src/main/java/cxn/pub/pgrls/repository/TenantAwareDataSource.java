package cxn.pub.pgrls.repository;

import cxn.pub.pgrls.TenantContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * This is the single place where RLS policies in the database are tied
 * to the application. We are using a connection session variable to tell
 * PostgreSQL what the current tenant context is for that connection.
 * Connections are not shared and session variables are private so this is
 * thread safe. If we did not use a session variable, you'd have to create
 * a Postgres login ROLE for each tenant and then maintain a lookup
 * mechanism to get the proper connection credentials for each tenant.
 */
public class TenantAwareDataSource extends AbstractRoutingDataSource {

	private static final Logger logger = LoggerFactory.getLogger(TenantAwareDataSource.class);
	
	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getTenant();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		// Every time the app asks the data source for a connection
		// set the PostgreSQL session variable to the current tenant
		// to enforce data isolation.
		Connection connection = super.getConnection();
		try (Statement sql = connection.createStatement()) {
			sql.execute("SET SESSION app.current_tenant = '" + determineCurrentLookupKey().toString() + "'");
		} catch (Exception e) {
			logger.error("Failed to execute: SET SESSION app.current_tenant = '" + determineCurrentLookupKey().toString() + "'");
			logger.error(e.getMessage());
		}
		return connection;
	}
}
