package hudson.plugins.backlog.repositorybrowser;

import hudson.model.AbstractProject;
import hudson.plugins.backlog.BacklogGitRepositoryBrowser;
import hudson.plugins.backlog.BacklogProjectProperty;
import hudson.plugins.backlog.BacklogRepositoryBrowser;
import hudson.scm.ChangeLogSet.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for {@link BacklogRepositoryBrowser} and
 * {@link BacklogGitRepositoryBrowser}.
 * 
 * @author ikikko
 */
public class RepositoryBrowserHelper {

	private static final Log LOG = LogFactory
			.getLog(RepositoryBrowserHelper.class);

	private String url;

	public RepositoryBrowserHelper(String url) {
		this.url = url;
	}

	/**
	 * Gets a Backlog project property configured for the current project.
	 */
	public BacklogProjectProperty getProjectProperty(Entry cs) {
		AbstractProject<?, ?> p = (AbstractProject<?, ?>) cs.getParent().build
				.getProject();

		return p.getProperty(BacklogProjectProperty.class);
	}

	public String getSpaceURL(Entry cs) {
		if (isDefaultUrl()) {
			BacklogProjectProperty property = getProjectProperty(cs);
			if (property == null || property.getSpaceURL() == null) {
				LOG.warn("BacklogProjectProperty is null or BacklogProjectProperty's spaceURL is null");
				return null;
			}
			return property.getSpaceURL();

		} else {
			if (!url.contains("/projects/")) {
				LOG.warn("Option project url is not correct");
				return null;
			}
			return url.substring(0, url.indexOf("/projects/") + 1);
		}
	}

	public String getProject(Entry cs) {
		if (isDefaultUrl()) {
			BacklogProjectProperty property = getProjectProperty(cs);
			if (property == null || property.getProject() == null) {
				LOG.warn("BacklogProjectProperty is null or BacklogProjectProperty's project is null");
				return null;
			}
			return property.getProject();

		} else {
			if (!url.contains("/projects/")) {
				LOG.warn("Option project url is not correct");
				return null;
			}
			return url.substring(url.indexOf("/projects/")
					+ "/projects/".length());
		}
	}

	public boolean isDefaultUrl() {
		return StringUtils.isEmpty(url);
	}

}
