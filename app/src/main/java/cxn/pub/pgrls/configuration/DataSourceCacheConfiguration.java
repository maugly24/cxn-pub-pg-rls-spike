package cxn.pub.pgrls.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates a singleton (shared Spring application wide) cache of active
 * tenants and their data source connection pools. In a more complete
 * solution, when a tenant logs out, they should be removed from this
 * cache so we can clean up their JDBC connection pool.
 * @see DataSourceRepository.java
 */
@Configuration
public class DataSourceCacheConfiguration {

	@Bean
	public Map<Object, Object> dataSourceTargets() {
		ConcurrentHashMap<Object, Object> targets = new ConcurrentHashMap<>();
		return targets;
	}

}
