
package cxn.pub.pgrls;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class TenantTest {

	@Test
	public void isLightweight() {
		Tenant lightweight = new Tenant();
		lightweight.setId(UUID.randomUUID());
		assertTrue("Only ID is lightweight", lightweight.isLightweight());

		lightweight.setUsers(new ArrayList<>());
		assertTrue("Only ID and empty users list is still lightweight", lightweight.isLightweight());

		Tenant heavyweight = new Tenant();
		heavyweight.setId(UUID.randomUUID());
		heavyweight.setName("ABCDEF");

		assertFalse("Any property in addition to ID are not lightweight", heavyweight.isLightweight());

		heavyweight.setStatus(Status.active);
		heavyweight.setTier(Tier.gold);
		heavyweight.setUsers(new ArrayList<>());
		assertFalse("Fully hydrated tenants are not lightweight", heavyweight.isLightweight());
	}
}