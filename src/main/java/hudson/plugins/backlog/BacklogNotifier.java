package hudson.plugins.backlog;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.Issue;
import com.nulabinc.backlog4j.IssueType;
import com.nulabinc.backlog4j.Project;
import com.nulabinc.backlog4j.api.option.CreateIssueParams;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.backlog.api.v2.BacklogClientFactory;
import hudson.tasks.*;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nulabinc.backlog4j.Issue.PriorityType;

/**
 * Notifier that creates issue on Backlog.
 * 
 * @author ikikko
 */
public class BacklogNotifier extends Notifier {

	private static class MessageCreator extends MailSender {

		private static final Pattern URL_MATCH_PATTERN = Pattern.compile(
				"(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",
				Pattern.CASE_INSENSITIVE);

		private AbstractBuild<?, ?> build;
		private BuildListener listener;

		public MessageCreator(AbstractBuild<?, ?> build, BuildListener listener) {
			super("", false, false); // dummy parameters
			this.build = build;
			this.listener = listener;
		}

		public MimeMessage getMessage() throws MessagingException,
				InterruptedException, IOException {
			MimeMessage message = getMail(build, listener);

			// add spaces back and forth all links (for Backlog Wiki notation)
			Matcher matcher = URL_MATCH_PATTERN.matcher(message.getContent()
					.toString());
			String textWithSpaces = matcher.replaceAll(" $0 ");
			message.setText(textWithSpaces);

			return message;
		}

	}

	@DataBoundConstructor
	public BacklogNotifier() {
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		if (build.getResult() == Result.SUCCESS) {
			return true;
		}

		// notify when build is broken at first.
		AbstractBuild<?, ?> pb = build.getPreviousBuild();
		if (pb == null || pb.getResult() != Result.SUCCESS) {
			listener.getLogger()
					.println(
							"Backlog issue is created only at first, so creating issue is skipped.");
			return true;
		}

		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);

		// check project property parameter
		if (bpp == null) {
			listener.getLogger()
					.println(
							"'Backlog property' is not set, so creating issue is skipped.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getSpaceURL())) {
			listener.getLogger().println(
					"'Backlog URL' is not set, so creating issue is skipped.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getProject())) {
			listener.getLogger()
					.println(
							"'project' is not included in Backlog URL, so creating issue is skipped.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.getApiKey())) {
			listener.getLogger().println(
					"'apiKey' is not set, so creating issue is skipped.");
			return true;
		}

		try {
			BacklogClient backlog = BacklogClientFactory.getBacklogClient(bpp);
			Issue issue = backlog.createIssue(buildCreateIssueParams(build, listener, bpp, backlog));

			listener.getLogger().println(
					"Created issue is [" + issue.getIssueKey() + "] : "
							+ bpp.getSpaceURL() + "view/" + issue.getIssueKey());

		} catch (MessagingException e) {
			e.printStackTrace(listener.error(e.getMessage()));
		}

		return true;
	}

	private CreateIssueParams buildCreateIssueParams(AbstractBuild<?, ?> build, BuildListener listener,
													 BacklogProjectProperty bpp, BacklogClient backlog)
			throws MessagingException, InterruptedException, IOException {
		Project project = backlog.getProject(bpp.getProject());

		MimeMessage message = new MessageCreator(build, listener).getMessage();

		IssueType firstIssueType = backlog.getIssueTypes(bpp.getProject()).get(0);

		PriorityType priorityType = PriorityType.Low;
		if (build.getResult() == Result.FAILURE) {
            priorityType = PriorityType.High;
        } else if (build.getResult() == Result.UNSTABLE) {
            priorityType = PriorityType.Normal;
        }

		CreateIssueParams createIssueParams =
				new CreateIssueParams(project.getId(), message.getSubject(), firstIssueType.getId(), priorityType);
		createIssueParams.description(message.getContent().toString());

		return createIssueParams;
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
			return Messages.BacklogNotifier_DisplayName();
		}

	}

}
