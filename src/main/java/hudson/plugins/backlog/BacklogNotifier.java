package hudson.plugins.backlog;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
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
		if (StringUtils.isEmpty(bpp.userId)) {
			listener.getLogger().println(
					"'userId' is not set, so creating issue is skipped.");
			return true;
		}
		if (StringUtils.isEmpty(bpp.password)) {
			listener.getLogger().println(
					"'password' is not set, so creating issue is skipped.");
			return true;
		}

		try {
			BacklogApiClient client = new BacklogApiClient();
			client.login(bpp.getSpaceURL(), bpp.userId, bpp.password);

			Project project = client.getProject(bpp.getProject());
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

		public FormValidation doCheckUserId(@QueryParameter String userId) {
			if (StringUtils.isEmpty(userId) || userId.matches("[A-Za-z0-9-_]+")) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages
						.BacklogNotifier_UserId_Error());
			}
		}

	}

}
