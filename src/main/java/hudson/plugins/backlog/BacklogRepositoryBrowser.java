package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.backlog.repositorybrowser.RepositoryBrowserHelper;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionRepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link SubversionRepositoryBrowser} that produces Backlog links.
 * 
 * @author ikikko
 */
public class BacklogRepositoryBrowser extends SubversionRepositoryBrowser {

	public final String url;

	@DataBoundConstructor
	public BacklogRepositoryBrowser(String url) {
		this.url = url;
	}

	@Override
	public URL getDiffLink(Path path) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (path.getEditType() != EditType.EDIT) {
			return null; // no diff if this is not an edit change
		}

		LogEntry logEntry = path.getLogEntry();
		if (helper.getSpaceURL(logEntry) == null
				|| helper.getProject(logEntry) == null) {
			return null;
		}

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		return new URL(helper.getSpaceURL(logEntry)
				+ "ViewRepositoryFileDiff.action" + "?projectKey="
				+ helper.getProject(logEntry) + "&path=" + encodedPath
				+ "&fromRevision=" + "-1" + "&toRevision="
				+ logEntry.getRevision());
	}

	@Override
	public URL getFileLink(Path path) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (path.getEditType() == EditType.DELETE) {
			return null;
		}

		LogEntry logEntry = path.getLogEntry();
		if (helper.getSpaceURL(logEntry) == null
				|| helper.getProject(logEntry) == null) {
			return null;
		}

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		return new URL(helper.getSpaceURL(logEntry)
				+ "ViewRepositoryFile.action" + "?projectKey="
				+ helper.getProject(logEntry) + "&r=" + logEntry.getRevision()
				+ "&path=" + encodedPath);
	}

	@Override
	public URL getChangeSetLink(LogEntry changeSet) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (helper.getSpaceURL(changeSet) == null
				|| helper.getProject(changeSet) == null) {
			return null;
		}

		return new URL(helper.getSpaceURL(changeSet) + "rev/"
				+ helper.getProject(changeSet) + "/" + changeSet.getRevision());
	}

	@Extension(optional = true)
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
