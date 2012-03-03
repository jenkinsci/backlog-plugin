package hudson.plugins.backlog;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.backlog.webdav.WebdavClient;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;

public class BacklogWebdavPublisher extends Notifier {

	private static final Log LOG = LogFactory
			.getLog(BacklogWebdavPublisher.class);

	public final String sourceFiles;
	public final String removePrefix;
	public final String remoteDirectory;
	public final boolean deleteDirectoryBeforePublish;

	@DataBoundConstructor
	public BacklogWebdavPublisher(String sourceFiles, String removePrefix,
			String remoteDirectory, boolean deleteDirectoryBeforePublish) {
		this.sourceFiles = sourceFiles;
		this.removePrefix = removePrefix;
		this.remoteDirectory = remoteDirectory;
		this.deleteDirectoryBeforePublish = deleteDirectoryBeforePublish;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	// TODO confirm on slave
	// TODO confirm on windows

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		if (build.getResult().isWorseThan(Result.UNSTABLE)) {
			LOG.info("WebDAV Publisher is not performed because build is not success or unstable.");
			return true;
		}

		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);

		WebdavClient client = new WebdavClient(bpp.getSpaceURL() + "dav/"
				+ bpp.getProject() + "/", bpp.userId, bpp.password);
		client.setRemovePrefix(removePrefix);

		if (deleteDirectoryBeforePublish) {
			LOG.debug("delete directory : " + getRemoteDirectoryWithSlash());
			client.delete(getRemoteDirectoryWithSlash());
		}

		String includes = build.getEnvironment(listener).expand(sourceFiles);
		for (FilePath filePath : build.getWorkspace().list(includes)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("put file : " + filePath);
			}
			client.putWithParent(filePath, getRemoteDirectoryWithSlash(),
					build.getWorkspace());
		}

		return true;
	}

	private String getRemoteDirectoryWithSlash() {
		if (remoteDirectory.endsWith("/")) {
			return remoteDirectory;
		} else {
			return remoteDirectory + "/";
		}
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Backlog Publisher to File";
		}

	}

}
