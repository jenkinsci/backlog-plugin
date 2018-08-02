package hudson.plugins.backlog;

import hudson.model.Action;

public class BacklogLinkAction implements Action {

	protected final BacklogProjectProperty prop;

	public BacklogLinkAction(BacklogProjectProperty prop) {
		this.prop = prop;
	}

	private boolean isEmptyPropUrl() {
		return prop == null || prop.url == null;
	}

	public String getIconFileName() {
		return isEmptyPropUrl() ? null : "/plugin/backlog/icon.png";
	}

	public String getDisplayName() {
		return isEmptyPropUrl() ? null : "Backlog";
	}

	public String getUrlName() {
		return isEmptyPropUrl() ? null : prop.url;
	}

}
