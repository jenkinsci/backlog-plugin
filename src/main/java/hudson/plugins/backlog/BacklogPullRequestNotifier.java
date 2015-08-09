package hudson.plugins.backlog;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.PullRequest;
import com.nulabinc.backlog4j.api.option.AddPullRequestCommentParams;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Notifier that comments a pull request on Backlog.
 * 
 * @author ikikko
 */
public class BacklogPullRequestNotifier extends Notifier {

	@DataBoundConstructor
	public BacklogPullRequestNotifier() {
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);

		// check project property parameter
		if (bpp == null) {
			listener.getLogger().println(
					"'Backlog property' is not set. Can't comment a pull request.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getSpaceURL())) {
			listener.getLogger().println(
					"'Backlog URL' is not set. Can't comment a pull request.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getProject())) {
			listener.getLogger().println(
					"'project' is not included in Backlog URL. Can't comment a pull request.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getApiKey())) {
			listener.getLogger().println(
					"'apiKey' is not set. Can't comment a pull request.");
			return true;
		}

		// TODO ikikko : check if git plugin is available
		if (!(build.getProject().getScm() instanceof GitSCM)) {
			listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
			return true;
		}

		BuildData data = build.getAction(BuildData.class);
		if (data == null) {
			listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
			return true;
		}

		// TODO ikikko : enable to change space
		String spaceKey = "nulab";
		BacklogConfigure configure = new BacklogJpConfigure(spaceKey).apiKey(bpp.getApiKey());
		BacklogClient backlog = new BacklogClientFactory(configure).newClient();

		for (RemoteConfig repository : ((GitSCM) build.getProject().getScm()).getRepositories()) {
			Pattern pullRequestRefPattern = Pattern.compile(getRefSpecDestination(repository) + "/(\\d+)/head");

			for (URIish uri : repository.getURIs()) {
				for (Branch branch : data.getLastBuiltRevision().getBranches()) {
					Matcher matcher = pullRequestRefPattern.matcher(branch.getName());
					if (!matcher.matches()) {
						continue;
					}

					long pullRequestId = Long.parseLong(matcher.group(1));
					PullRequest pullRequest = backlog.getPullRequest(bpp.getProject(), uri.getHumanishName(), pullRequestId);
					if (!pullRequest.getStatus().getStatus().equals(PullRequest.StatusType.Open)) {
						continue;
					}

					String content = String.format("Build is %s ( %s )",
							build.getResult().toString(), build.getAbsoluteUrl());
					AddPullRequestCommentParams AddParams = new AddPullRequestCommentParams(
							bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber(), content);
					backlog.addPullRequestComment(AddParams);

					listener.getLogger().println(String.format("pull request comment : %s#%d",
							uri.getHumanishName(), pullRequest.getNumber()));
				}
			}
		}

		return true;
	}

	// resSpec : +refs/pull/*:refs/remotes/origin/pr/*
	// dest    : origin/pr
	private String getRefSpecDestination(RemoteConfig repository) {
		RefSpec refSpec = repository.getFetchRefSpecs().get(0);

		String destination = refSpec.getDestination().substring(Constants.R_REMOTES.length());
		return destination.replace("/*", "");
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		@Override
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.BacklogPullRequestNotifier_DisplayName();
		}

	}

}
