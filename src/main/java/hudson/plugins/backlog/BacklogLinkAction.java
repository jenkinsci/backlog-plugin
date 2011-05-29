package hudson.plugins.backlog;

import hudson.model.Action;

public class BacklogLinkAction implements Action {

	private final BacklogProjectProperty prop;

	public BacklogLinkAction(BacklogProjectProperty prop) {
		this.prop = prop;
	}

	public String getIconFileName() {
		return "/plugin/backlog/icon.png";
	}

	public String getDisplayName() {
		return "Backlog";
	}

	public String getUrlName() {
		return prop.spaceURL;
	}

}
