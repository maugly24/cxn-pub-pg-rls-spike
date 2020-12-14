package cxn.pub.pgrls;

/**
 * Simplistic approach to maintain thread safe tenant context
 */
public class TenantContext {
	
	private static final ThreadLocal<Object> TENANT = new ThreadLocal<>();
	
	public static Object getTenant() {
		return TENANT.get();
	}
	
	public static void setTenant(Object tenant) {
		TENANT.set(tenant);
	}
	
}
