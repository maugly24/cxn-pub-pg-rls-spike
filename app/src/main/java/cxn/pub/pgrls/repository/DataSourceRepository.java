package cxn.pub.pgrls.repository;

import cxn.pub.pgrls.TenantContext;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

/**
 * Generates a JDBC connection pool per authenticated tenant. These
 * connections will be constrained by RLS policies to prevent cross
 * tenant data access. In a more complete solution, when a tenant
 * logs out, reference to that tenant in the data source should be
 * removed so the pool of connections can be garbage collected.
 *
 * Most systems have multiple users per tenant. These connection
 * pools are per tenant, not user.
 */
@Repository
@Configuration
public class DataSourceRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(DataSourceRepository.class);

	// See DataSourcePropertiesConfiguration
	@Autowired
	@Qualifier("dataSourceProperties")
	private DataSourceProperties dataSourceProperties;
	
	// See DataSourceCacheConfiguration
	@Autowired
	private Map<Object, Object> dataSourceTargets;

	private final TenantAwareDataSource dataSource = new TenantAwareDataSource();

	public javax.sql.DataSource dataSource() {
		Object currentTenant = TenantContext.getTenant();
		if (currentTenant == null) {
			throw new RuntimeException("No current tenant");
		}

		// Each tenant gets its own Hikari connection pool
		if (!dataSourceTargets.containsKey(currentTenant) || dataSourceTargets.get(currentTenant) == null) {
			dataSourceTargets.put(currentTenant, dataSourceProperties.initializeDataSourceBuilder().build());
		}

		// Tell our data source router where to find all the keys
		// we want it to map pools to
		dataSource.setTargetDataSources(dataSourceTargets);

		// Tell Spring we're done configuring the data source so
		// it can initialize the routing functionality. We must call
		// this each time, because internally AbstractRoutingDataSource
		// is keeping a map of resolved data sources per key and we
		// may have just added a new tenant to our list of targets or
		// we may have removed (logged out) a tenant and want to cleanup.
		dataSource.afterPropertiesSet();

//		logger.info("Returning dataSource with targets:");
//		dataSourceTargets.keySet().forEach((key) -> {
//			logger.info(String.valueOf(key));
//		});
		
		return dataSource;
	}

	public Map<Object, Object> getDataSourceTargets() {
		return dataSourceTargets;
	}
}
