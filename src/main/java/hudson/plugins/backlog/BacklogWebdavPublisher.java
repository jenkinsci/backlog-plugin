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

	public final String artifacts;

	@DataBoundConstructor
	public BacklogWebdavPublisher(String artifacts) {
		this.artifacts = artifacts;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	// FIXME confirm on slave

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

		// String includes = build.getEnvironment(listener).expand(artifacts);
		String includes = build.getEnvironment(listener).expand(
				"**/src/**/java/**/*");
		FilePath[] filePaths = build.getWorkspace().list(includes);

		listener.getLogger().println("***** " + build.getWorkspace());
		for (FilePath filePath : filePaths) {
			listener.getLogger().println("***** " + filePath);

			client.putWithParent(filePath, "BacklogPublisher/",
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
