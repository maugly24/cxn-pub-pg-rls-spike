package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Tenant;

import java.util.List;

/**
 * Simplistic CRUD API
 */
public interface AdminService {

	public Tenant registerTenant(Tenant tenant);

	public List<Tenant> getTenants();

	public void deleteTenant(Tenant tenant);

	public void deleteTenantUsers(Tenant tenant);
}
