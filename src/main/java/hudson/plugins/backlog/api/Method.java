package hudson.plugins.backlog.api;

import hudson.plugins.backlog.api.entity.Issue;
import hudson.plugins.backlog.api.entity.Project;
import hudson.plugins.backlog.api.entity.User;

/**
 * Backlog API Method.
 * 
 * @author ikikko
 */
public enum Method {

	/** Get a {@link User} specified by {@code userId}. */
	GET_USER("backlog.getUser"),

	/** Get a {@link Project} specified by {@code key}. */
	GET_PROJECT("backlog.getProject"),

	/** Create a {@link Issue}. Return the created issue if success. */
	CREATE_ISSUE("backlog.createIssue");

	private String name;

	private Method(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
