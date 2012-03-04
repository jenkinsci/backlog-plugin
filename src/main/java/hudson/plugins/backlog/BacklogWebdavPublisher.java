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
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;

public class BacklogWebdavPublisher extends Notifier {

	// TODO confirm on slave
	// TODO confirm on windows

	private static final Log LOG = LogFactory
			.getLog(BacklogWebdavPublisher.class);

	public final String sourceFiles;
	public final String remoteDirectory;
	public final String removePrefixDirectory;
	public final boolean remoteDirectorySDF;
	public final boolean deleteDirectoryBeforePublish;

	@DataBoundConstructor
	public BacklogWebdavPublisher(String sourceFiles, String remoteDirectory,
			String removePrefixDirectory, boolean remoteDirectorySDF,
			boolean deleteDirectoryBeforePublish) {
		this.sourceFiles = sourceFiles;
		this.remoteDirectory = remoteDirectory;
		this.removePrefixDirectory = removePrefixDirectory;
		this.remoteDirectorySDF = remoteDirectorySDF;
		this.deleteDirectoryBeforePublish = deleteDirectoryBeforePublish;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		// skip if build is not success or unstable
		if (build.getResult().isWorseThan(Result.UNSTABLE)) {
			LOG.info("WebDAV Publisher is not performed because build is not success or unstable.");
			return true;
		}

		// webdav client
		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);
		WebdavClient client = new WebdavClient(bpp.getSpaceURL() + "dav/"
				+ bpp.getProject() + "/", bpp.userId, bpp.password);
		client.setRemovePrefixDirectory(removePrefixDirectory);

		// remote directory, if specified date format
		String directory = getFormattedRemoteDirectory(build, listener,
				remoteDirectory);
		LOG.debug("remote directory : " + directory);

		// delete remote directory
		if (deleteDirectoryBeforePublish && !directory.isEmpty()) {
			if (client.delete(directory)) {
				LOG.debug("delete remote directory : " + directory);
			}
		}

		// put target files to Backlog files
		String includes = build.getEnvironment(listener).expand(sourceFiles);
		for (FilePath filePath : build.getWorkspace().list(includes)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("put file : " + filePath);
			}
			client.putWithParent(filePath, directory, build.getWorkspace());
		}

		return true;
	}

	private String getFormattedRemoteDirectory(AbstractBuild<?, ?> build,
			BuildListener listener, String remoteDirectory) throws IOException,
			InterruptedException {
		if (remoteDirectorySDF) {
			String expandRemoteDirectory = build.getEnvironment(listener)
					.expand(remoteDirectory);
			SimpleDateFormat sdf = new SimpleDateFormat(expandRemoteDirectory);

			return sdf.format(build.getTime());
		} else {
			return remoteDirectory;
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
