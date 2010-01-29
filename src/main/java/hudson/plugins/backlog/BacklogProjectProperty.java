package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Property for {@link AbstractProject} that stores the associated Backlog website URL.
 *
 * @see http://d.hatena.ne.jp/cactusman/20090328/p1
 * @author dragon3
 */
public final class BacklogProjectProperty extends JobProperty<AbstractProject<?, ?>> {

	public final String spaceURL;

	@DataBoundConstructor
	public BacklogProjectProperty(String spaceURL) {
		// normalize
		if (spaceURL == null || spaceURL.length() == 0)
			spaceURL = null;
		else {
			if (!spaceURL.endsWith("/"))
				spaceURL += '/';
		}
		this.spaceURL = spaceURL;
	}

    @Override
    public Action getJobAction(AbstractProject<?,?> job) {
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

		public String getDisplayName() {
			return "Associated Backlog website";
		}

		@Override
		public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			BacklogProjectProperty bpp = req.bindJSON(BacklogProjectProperty.class, formData);
			if (bpp.spaceURL == null)
				bpp = null; // not configured
			return bpp;
		}
    }
}
