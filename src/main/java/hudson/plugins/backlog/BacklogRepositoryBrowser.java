package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionRepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link SubversionRepositoryBrowser} that produces Backlog links.
 * 
 * @author ikikko
 */
public class BacklogRepositoryBrowser extends SubversionRepositoryBrowser {

	private static final Log LOG = LogFactory
			.getLog(BacklogRepositoryBrowser.class);

	public final String url;

	@DataBoundConstructor
	public BacklogRepositoryBrowser(String url) {
		this.url = url;
	}

	/**
	 * Gets a Backlog project property configured for the current project.
	 */
	BacklogProjectProperty getProjectProperty(LogEntry cs) {
		AbstractProject<?, ?> p = (AbstractProject<?, ?>) cs.getParent().build
				.getProject();

		return p.getProperty(BacklogProjectProperty.class);
	}

	String getSpaceURL(LogEntry cs) {
		if (isDefaultSvnUrl()) {
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

	String getProject(LogEntry cs) {
		if (isDefaultSvnUrl()) {
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

	boolean isDefaultSvnUrl() {
		return StringUtils.isEmpty(url);
	}

	@Override
	public URL getDiffLink(Path path) throws IOException {
		if (path.getEditType() != EditType.EDIT) {
			return null; // no diff if this is not an edit change
		}

		LogEntry logEntry = path.getLogEntry();
		if (getSpaceURL(logEntry) == null || getProject(logEntry) == null) {
			return null;
		}

		int revision = path.getLogEntry().getRevision();

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		return new URL(getSpaceURL(logEntry) + "ViewRepositoryFileDiff.action"
				+ "?projectKey=" + getProject(logEntry) + "&path="
				+ encodedPath + "&fromRevision=" + "-1" + "&toRevision="
				+ revision);
	}

	@Override
	public URL getFileLink(Path path) throws IOException {
		if (path.getEditType() == EditType.DELETE) {
			return null;
		}

		LogEntry logEntry = path.getLogEntry();
		if (getSpaceURL(logEntry) == null || getProject(logEntry) == null) {
			return null;
		}

		int revision = path.getLogEntry().getRevision();

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		return new URL(getSpaceURL(logEntry) + "ViewRepositoryFile.action"
				+ "?projectKey=" + getProject(logEntry) + "&r=" + revision
				+ "&path=" + encodedPath);
	}

	@Override
	public URL getChangeSetLink(LogEntry changeSet) throws IOException {
		if (getSpaceURL(changeSet) == null || getProject(changeSet) == null) {
			return null;
		}

		return new URL(getSpaceURL(changeSet) + "rev/" + getProject(changeSet)
				+ "/" + changeSet.getRevision());
	}

	@Extension
	public static final class DescriptorImpl extends
			Descriptor<RepositoryBrowser<?>> {
		public DescriptorImpl() {
			super(BacklogRepositoryBrowser.class);
		}

		public String getDisplayName() {
			return "Backlog";
		}
	}

}
