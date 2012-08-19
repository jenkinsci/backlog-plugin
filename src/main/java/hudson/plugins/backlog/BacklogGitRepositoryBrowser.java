package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.RepositoryBrowser;

import java.io.IOException;
import java.net.URL;

public class BacklogGitRepositoryBrowser extends GitRepositoryBrowser {

	@Override
	public URL getDiffLink(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getFileLink(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
