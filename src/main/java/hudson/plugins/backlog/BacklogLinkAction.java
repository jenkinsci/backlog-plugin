package hudson.plugins.backlog;

import hudson.model.Action;

public class BacklogLinkAction implements Action {

	private final BacklogProjectProperty prop;

	public BacklogLinkAction(BacklogProjectProperty prop) {
		this.prop = prop;
	}

	public String getIconFileName() {
		if (prop.url == null) {
			return null;
		}
		return "/plugin/backlog/icon.png";
	}

	public String getDisplayName() {
		if (prop.url == null) {
			return null;
		}
		return "Backlog";
	}

	public String getUrlName() {
		if (prop.url == null) {
			return null;
		}
		return prop.url;
	}

}
