package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Tenant;
import cxn.pub.pgrls.User;

import java.util.List;
import java.util.UUID;

/**
 * Simplistic CRUD API
 */
public interface TenantService {

	public Tenant getTenant(UUID tenantId);

	public Tenant saveTenant(Tenant tenant);

	public List<User> getUsers(Tenant tenant);

	public User saveUser(User user);

	public User getUser(UUID userId);

	public void logout(UUID tenantId);
}
