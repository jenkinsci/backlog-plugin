package hudson.plugins.backlog.api.entity;

import java.util.Date;

public class User {

	/** Name */
	private String name;

	/** ID */
	private int id;

	/** Updated date */
	private Date updatedOn;

	public User() {
	}

	public User(final String name, final int id, final Date updatedOn) {
		this.name = name;
		this.id = id;
		this.updatedOn = updatedOn;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(final Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", updatedOn=" + updatedOn
				+ "]";
	}

}
