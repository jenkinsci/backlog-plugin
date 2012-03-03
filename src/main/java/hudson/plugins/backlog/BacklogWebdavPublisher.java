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

import org.kohsuke.stapler.DataBoundConstructor;

public class BacklogWebdavPublisher extends Notifier {

	public final String sourceFiles;
	public final String removePrefix;
	public final String remoteDirectory;

	@DataBoundConstructor
	public BacklogWebdavPublisher(String sourceFiles, String removePrefix,
			String remoteDirectory) {
		this.sourceFiles = sourceFiles;
		this.removePrefix = removePrefix;
		this.remoteDirectory = remoteDirectory;
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
			return true;
		}

		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);

		WebdavClient client = new WebdavClient(bpp.getSpaceURL() + "dav/"
				+ bpp.getProject() + "/", bpp.userId, bpp.password);
		client.setRemovePrefix(removePrefix);

		String includes = build.getEnvironment(listener).expand(sourceFiles);
		FilePath[] filePaths = build.getWorkspace().list(includes);

		listener.getLogger().println("***** " + build.getWorkspace());
		for (FilePath filePath : filePaths) {
			listener.getLogger().println("***** " + filePath);

			client.putWithParent(filePath, remoteDirectory,
					build.getWorkspace());
		}

		return true;
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
