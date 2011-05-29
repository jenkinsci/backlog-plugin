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

	public final String spaceURL;
	public final String userId;
	public final String password;

	// TODO add help for userId/password

	@DataBoundConstructor
	public BacklogProjectProperty(final String spaceURL, final String userId,
			final String password) {

		// normalize
		if (StringUtils.isNotEmpty(spaceURL)) {
			if (spaceURL.contains("/projects/")) {
				this.spaceURL = spaceURL;
			} else if (spaceURL.endsWith("/")) {
				this.spaceURL = spaceURL;
			} else {
				this.spaceURL = spaceURL + '/';
			}
		} else {
			this.spaceURL = null;
		}

		this.userId = userId;
		this.password = password;
	}

	public String getSpaceURL2() {
		// TODO rename spaceURL
		if (spaceURL == null) {
			return null;
		}

		if (spaceURL.contains("/projects/")) {
			return spaceURL.substring(0, spaceURL.indexOf("/projects/") + 1);
		} else {
			return spaceURL;
		}

	}

	public String getProject() {
		if (spaceURL == null) {
			return null;
		}
		if (!spaceURL.contains("/projects/")) {
			return null;
		}

		return spaceURL.substring(spaceURL.indexOf("/projects/")
				+ "/projects/".length());
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

			BacklogProjectProperty bpp = req.bindJSON(
					BacklogProjectProperty.class, formData);
			if (bpp.spaceURL == null) // TODO need?
				bpp = null; // not configured
			return bpp;
		}
	}
}
