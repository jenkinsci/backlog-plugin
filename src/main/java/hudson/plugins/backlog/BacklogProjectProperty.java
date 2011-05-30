package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Job;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Property for {@link AbstractProject} that stores the associated Backlog
 * website URL.
 * 
 * @see http://d.hatena.ne.jp/cactusman/20090328/p1
 * @author dragon3
 */
public final class BacklogProjectProperty extends
		JobProperty<AbstractProject<?, ?>> {

	public final String url;
	public final String userId;
	public final String password;

	// TODO add help for userId/password

	@DataBoundConstructor
	public BacklogProjectProperty(final String url, final String userId,
			final String password) {

		// normalize
		if (StringUtils.isNotEmpty(url)) {
			if (url.contains("/projects/")) {
				this.url = url;
			} else if (url.endsWith("/")) {
				this.url = url;
			} else {
				this.url = url + '/';
			}
		} else {
			this.url = null;
		}

		this.userId = userId;
		this.password = password;
	}

	public String getSpaceURL() {
		if (url == null) {
			return null;
		}

		if (url.contains("/projects/")) {
			return url.substring(0, url.indexOf("/projects/") + 1);
		} else {
			return url;
		}

	}

	public String getProject() {
		if (url == null) {
			return null;
		}
		if (!url.contains("/projects/")) {
			return null;
		}

		return url.substring(url.indexOf("/projects/") + "/projects/".length());
	}

	@Override
	public Action getJobAction(AbstractProject<?, ?> job) {
		return new BacklogLinkAction(this);
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {

		public DescriptorImpl() {
			super(BacklogProjectProperty.class);
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return Messages.BacklogProjectProperty_DisplayName();
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {

			if (formData.isEmpty()) {
				return null;
			}

			BacklogProjectProperty bpp = req.bindJSON(
					BacklogProjectProperty.class,
					formData.getJSONObject("backlog"));
			return bpp;
		}
	}
}
