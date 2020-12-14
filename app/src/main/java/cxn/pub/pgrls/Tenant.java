package cxn.pub.pgrls;

import java.util.List;
import java.util.UUID;

public class Tenant {

	private UUID id;
	private String name;
	private Tier tier;
	private Status status;
	private List<User> users;

	public Tenant() {
		this(null);
	}

	public Tenant(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Tier getTier() {
		return tier;
	}

	public void setTier(Tier tier) {
		this.tier = tier;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	/**
	 * Returns true if only the only property set is the id
	 * @return
	 */
	public boolean isLightweight() {
		return (
			id != null
			&& (name == null || name.isEmpty())
			&& tier == null
			&& status == null
			&& (users == null || users.isEmpty())
		);
	}
}
