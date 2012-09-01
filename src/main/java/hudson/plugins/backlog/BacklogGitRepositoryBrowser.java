package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.backlog.repositorybrowser.RepositoryBrowserHelper;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * {@link GitRepositoryBrowser} that produces Backlog links.
 * 
 * @author ikikko
 */
public class BacklogGitRepositoryBrowser extends GitRepositoryBrowser {

	// TODO receive repo name
	private static String repoName = "backlog-plugin-test";

	public final String url;

	@DataBoundConstructor
	public BacklogGitRepositoryBrowser(String url) {
		this.url = url;
	}

	@Override
	public URL getDiffLink(Path path) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (path.getEditType() != EditType.EDIT) {
			return null; // no diff if this is not an edit change
		}

		GitChangeSet logEntry = path.getChangeSet();
		if (helper.getSpaceURL(logEntry) == null
				|| helper.getProject(logEntry) == null) {
			return null;
		}

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		// TODO what is commit id when merge commit ?

		return new URL(helper.getSpaceURL(logEntry) + "git/"
				+ helper.getProject(logEntry) + "/" + repoName + "/diff/"
				+ logEntry.getParentCommit() + "..." + logEntry.getCommitId()
				+ "/" + encodedPath);
	}

	@Override
	public URL getFileLink(Path path) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (path.getEditType() == EditType.DELETE) {
			return null;
		}

		GitChangeSet logEntry = path.getChangeSet();
		if (helper.getSpaceURL(logEntry) == null
				|| helper.getProject(logEntry) == null) {
			return null;
		}

		String filePath = path.getPath();
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		String encodedPath = URLEncoder.encode(filePath, "UTF-8");

		return new URL(helper.getSpaceURL(logEntry) + "git/"
				+ helper.getProject(logEntry) + "/" + repoName + "/blob/"
				+ logEntry.getCommitId() + "/" + encodedPath);
	}

	@Override
	public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(url);

		if (helper.getSpaceURL(changeSet) == null
				|| helper.getProject(changeSet) == null) {
			return null;
		}

		return new URL(helper.getSpaceURL(changeSet) + "git/"
				+ helper.getProject(changeSet) + "/" + repoName + "/"
				+ "commit/" + changeSet.getRevision());
	}

	@Extension
	public static final class DescriptorImpl extends
			Descriptor<RepositoryBrowser<?>> {
		public DescriptorImpl() {
			super(BacklogGitRepositoryBrowser.class);
		}

		public String getDisplayName() {
			return "Backlog";
		}
	}

}
