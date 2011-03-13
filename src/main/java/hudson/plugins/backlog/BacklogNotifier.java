package hudson.plugins.backlog;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.backlog.api.BacklogApiClient;
import hudson.plugins.backlog.api.entity.Issue;
import hudson.plugins.backlog.api.entity.Priority;
import hudson.plugins.backlog.api.entity.Project;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.MailSender;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Notifier that creates issue on Backlog.
 * 
 * @author ikikko
 */
public class BacklogNotifier extends Notifier {

	public final String space;

	public final String projectKey;

	public final String userId;

	public final String password;

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
	public BacklogNotifier(String space, String projectKey, String userId,
			String password) {
		this.space = space;
		this.projectKey = projectKey;
		this.userId = userId;
		this.password = password;
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

		try {
			BacklogApiClient client = new BacklogApiClient();
			client.login(space, userId, password);

			Project project = client.getProject(projectKey);
			MimeMessage message = new MessageCreator(build, listener)
					.getMessage();

			Issue newIssue = new Issue();
			newIssue.setSummary(message.getSubject());
			newIssue.setDescription(message.getContent().toString());
			if (build.getResult() == Result.FAILURE) {
				newIssue.setPriority(Priority.HIGH);
			} else if (build.getResult() == Result.UNSTABLE) {
				newIssue.setPriority(Priority.MIDDLE);
			}
			Issue issue = client.createIssue(project.getId(), newIssue);

			listener.getLogger().println(
					"Created issue is [" + issue.getKey() + "] : "
							+ issue.getUrl());

		} catch (XmlRpcException e) {
			e.printStackTrace(listener.error(e.getMessage()));
		} catch (MessagingException e) {
			e.printStackTrace(listener.error(e.getMessage()));
		}

		return true;
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

		public FormValidation doCheckSpace(@QueryParameter String space) {
			if (StringUtils.isEmpty(space) || space.matches("[a-z0-9-]{3,10}")) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages
						.BacklogNotifier_Space_Error());
			}
		}

		public FormValidation doCheckProjectKey(
				@QueryParameter String projectKey) {
			if (StringUtils.isEmpty(projectKey)
					|| projectKey.matches("[A-Z0-9]+")) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages
						.BacklogNotifier_ProjectKey_Error());
			}
		}

		public FormValidation doCheckUserId(@QueryParameter String userId) {
			if (StringUtils.isEmpty(userId) || userId.matches("[A-Za-z0-9-_]+")) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages
						.BacklogNotifier_UserId_Error());
			}
		}

		public FormValidation doCreateTestIssue(@QueryParameter String space,
				@QueryParameter String projectKey,
				@QueryParameter String userId, @QueryParameter String password) {

			// not created if all field is not filled
			if (StringUtils.isEmpty(space) || StringUtils.isEmpty(projectKey)
					|| StringUtils.isEmpty(userId)
					|| StringUtils.isEmpty(password)) {
				return FormValidation.warning(Messages
						.BacklogNotifier_CreateTestIssue_Skip());
			}

			try {
				BacklogApiClient client = new BacklogApiClient();
				client.login(space, userId, password);

				Project project = client.getProject(projectKey);
				Issue newIssue = new Issue();
				newIssue.setSummary("Test issue");
				newIssue.setDescription("This is test issue created by "
						+ Hudson.getInstance().getDisplayName());

				Issue issue = client.createIssue(project.getId(), newIssue);

				return FormValidation
						.okWithMarkup(Messages
								.BacklogNotifier_CreateTestIssue_Success(issue
										.getUrl()));
			} catch (Exception e) {
				return FormValidation.errorWithMarkup(Messages
						.BacklogNotifier_CreateTestIssue_Failure(Functions
								.printThrowable(e)));
			}
		}
	}

}
