package hudson.plugins.backlog;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.PullRequest;
import com.nulabinc.backlog4j.api.option.AddPullRequestCommentParams;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogPackageConfigure;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
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

		if (Jenkins.getInstance().getPlugin("git") == null) {
			listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
			return true;
		}

		BuildData data = build.getAction(BuildData.class);
		if (data == null) {
			listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
			return true;
		}

		listener.getLogger().println("Adding pull request comments...");

		BacklogConfigure configure = new BacklogPackageConfigure(bpp.getSpaceURL()).apiKey(bpp.getApiKey());
		BacklogClient backlog = new BacklogClientFactory(configure).newClient();

		for (RemoteConfig repository : ((GitSCM) build.getProject().getScm()).getRepositories()) {
			Pattern pullRequestRefPattern;
			try {
				pullRequestRefPattern = getPullRequestRefPattern(repository);
			} catch (IllegalArgumentException e) {
				listener.getLogger().println(e.getMessage());
				continue;
			}

			for (URIish uri : repository.getURIs()) {
				for (Branch branch : data.getLastBuiltRevision().getBranches()) {
					Matcher matcher = pullRequestRefPattern.matcher(branch.getName());
					if (!matcher.matches()) {
						continue;
					}

					long pullRequestId = Long.parseLong(matcher.group("number"));
					PullRequest pullRequest = backlog.getPullRequest(bpp.getProject(), uri.getHumanishName(), pullRequestId);

					if (!pullRequest.getStatus().getStatus().equals(PullRequest.StatusType.Open)) {
						listener.getLogger().print("This pull request has been already closed : ");
						hyperlinkPullRequest(listener, bpp, uri, pullRequest);
						continue;
					}

					String content = String.format("%s Build %s ( %s )",
							convertEmoticonFromResult(build.getResult()), build.getResult().toString(), build.getAbsoluteUrl());
					AddPullRequestCommentParams AddParams = new AddPullRequestCommentParams(
							bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber(), content);
					backlog.addPullRequestComment(AddParams);

					listener.getLogger().print("Added a pull request comment : ");
					hyperlinkPullRequest(listener, bpp, uri, pullRequest);
				}
			}
		}

		return true;
	}

	// refSpec     : +refs/pull/*:refs/remotes/origin/pr/*
	// destination : origin/pr
	//
	// result      : (refs/remotes/)?origin/pr/(\\d+)/head
	Pattern getPullRequestRefPattern(RemoteConfig repository) {
		RefSpec refSpec = repository.getFetchRefSpecs().get(0);

		String source = refSpec.getSource();
		if (!source.contains("refs/pull/")) {
			throw new IllegalArgumentException("Don't add pull request comments, because refspec's source '" + source + "' doesn't contain pull request refs 'refs/pull/'");
		}

		String destination = refSpec.getDestination().substring(Constants.R_REMOTES.length());
		destination = destination.replace("/*", "");

		return Pattern.compile(String.format("(refs/remotes/)?%s/(?<number>\\d+)/head", destination));
	}

	private void hyperlinkPullRequest(BuildListener listener, BacklogProjectProperty bpp, URIish uri, PullRequest pullRequest) throws IOException {
		String url = String.format("%sgit/%s/%s/pullRequests/%d",
				bpp.getSpaceURL(), bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber());
		String text = String.format("%s/%s#%d\n",
				bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber());

		listener.hyperlink(url, text);
	}

	private String convertEmoticonFromResult(Result result) {
		if (result.isBetterOrEqualTo(Result.SUCCESS)) {
			return "\":-D\"";
		} else if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
			return "\":'(\"";
		} else if (result.isBetterOrEqualTo(Result.FAILURE)) {
			return "\":-@\"";
		} else {
			return "\":-S\"";
		}
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
