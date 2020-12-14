package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Tenant;
import cxn.pub.pgrls.TenantContext;
import cxn.pub.pgrls.UnauthorizedException;
import cxn.pub.pgrls.User;
import cxn.pub.pgrls.repository.DataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * In a more complete solution, you'd break up your business logic
 * and error handling here and move the data access code to another
 * set of interfaces.
 */
@Service
public class TenantServiceImpl implements TenantService {

	private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

	@Autowired
	private DataSourceRepository repo;
	private final JdbcTemplate jdbc = new JdbcTemplate();

	// We can reuse the JDBC Template instance, but we
	// have to ask the repository for the data source each
	// time to ensure that the current tenant context is
	// reflected.
	private JdbcTemplate jdbc() {
		jdbc.setDataSource(repo.dataSource());

		logger.info("Spring current tenant = " + TenantContext.getTenant());
		try {
			Connection conn = jdbc.getDataSource().getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW app.current_tenant");
			rs.next();
			String connectionCurrentTenant = rs.getString(1);
			logger.info("Postgres current tenant = " + connectionCurrentTenant);
		} catch (Exception e) {
		}

		return jdbc;
	}

	/**
	 * Notice that there is nothing special about these queries.
	 * RLS protection is transparent to us because it's managed
	 * in the connection.
	 * @param tenantId
	 * @return
	 */
	@Override
	public Tenant getTenant(UUID tenantId) {
		Tenant tenant = null;
		try {
			tenant = jdbc().queryForObject("SELECT tenant_id, name, status, tier FROM tenant WHERE tenant_id = ?", new TenantRowMapper(), tenantId);
			tenant.setUsers(getUsers(tenant));
		} catch (EmptyResultDataAccessException e) {
			// If row level security policies aren't met, it's not
			// an exception from the database, it's just as if the
			// data didn't exist in the table.
		}
		return tenant;
	}

	@Override
	public Tenant saveTenant(Tenant tenant) {
		Tenant saved = null;
		int updated = jdbc().update("UPDATE tenant SET name = ?, status = ?, tier = ? WHERE tenant_id = ?", tenant.getName(), tenant.getStatus(), tenant.getTier(), tenant.getId());
		if (updated == 1) {
			saved = getTenant(tenant.getId());
		}
		return saved;
	}

	@Override
	public List<User> getUsers(Tenant tenant) {
		List<User> users = new ArrayList<>();
		try {
			users = jdbc().query("SELECT tenant_id, user_id, email, given_name, family_name FROM tenant_user WHERE tenant_id = ?", new UserRowMapper(), tenant.getId());
		} catch (EmptyResultDataAccessException e) {
			// If row level security policies aren't met, it's not
			// an exception from the database, it's just as if the
			// data didn't exist in the table.
		}
		return users;
	}

	@Override
	public User getUser(UUID userId) {
		User user = null;
		try {
			user = jdbc().queryForObject("SELECT tenant_id, user_id, email, given_name, family_name FROM tenant_user WHERE user_id = ?", new UserRowMapper(), userId);
		} catch (EmptyResultDataAccessException e) {
			// If row level security policies aren't met, it's not
			// an exception from the database, it's just as if the
			// data didn't exist in the table.
		}
		return user;
	}

	@Override
	public User saveUser(User user) {
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(jdbc());
		GeneratedKeyHolder generated = new GeneratedKeyHolder();
		StringBuilder sql = new StringBuilder("INSERT INTO tenant_user (tenant_id, email, given_name, family_name) VALUES (:tenant_id, :email, :given_name, :family_name)");
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("tenant_id", user.getTenant().getId())
				.addValue("email", user.getEmail())
				.addValue("given_name", user.getGivenName())
				.addValue("family_name", user.getFamilyName());
		try {
			int update = jdbc.update(sql.toString(), params, generated);
			if (update == 1) {
				UUID userId = (UUID) generated.getKeys().get("user_id");
				user.setId(userId);
				user.setTenant(getTenant(user.getTenant().getId()));
			}
		} catch (BadSqlGrammarException e) {
			// Postgres will throw an Access Rule Violation error with condition
			// insufficient_privilege if an INSERT fails to satisfy an RLS policy.
			// ERROR:  42501: new row violates row-level security policy for table...
			if ("42501".equals(e.getSQLException().getSQLState())) {
				throw new UnauthorizedException();
			} else {
				throw e;
			}
		}
		return user;
	}

	public void logout(UUID tenantId) {
		repo.getDataSourceTargets().remove(tenantId);
	}
}
