package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Tenant;
import cxn.pub.pgrls.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRowMapper implements RowMapper<User> {

	public User mapRow(ResultSet result, int rowNumber) throws SQLException {
		User user = new User();
		user.setId(result.getObject("user_id", UUID.class));
		user.setEmail(result.getString("email"));
		user.setFamilyName(result.getString("family_name"));
		user.setGivenName(result.getString("given_name"));
		Tenant tenant = new Tenant();
		tenant.setId(result.getObject("tenant_id", UUID.class));
		user.setTenant(tenant);
		return user;
	}
}
