package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Status;
import cxn.pub.pgrls.Tenant;
import cxn.pub.pgrls.Tier;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TenantRowMapper implements RowMapper<Tenant> {

	public Tenant mapRow(ResultSet result, int rowNumber) throws SQLException {
		Tenant tenant = new Tenant();
		tenant.setId(result.getObject("tenant_id", UUID.class));
		tenant.setName(result.getString("name"));
		String s = result.getString("status");
		if (s != null) {
			tenant.setStatus(Status.valueOf(s));
		}
		String t = result.getString("tier");
		if (t != null) {
			tenant.setTier(Tier.valueOf(t));
		}
		return tenant;
	}
}
