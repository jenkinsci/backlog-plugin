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

    public final String projectURL;
	public final String spaceURL;

	@DataBoundConstructor
	public BacklogProjectProperty(final String spaceURL) {
        String tempS = null;
        String tempP = null;
		// normalize
		if (spaceURL != null && spaceURL.length() > 0) {
            if (spaceURL.indexOf("/projects/") > -1) {
                tempS = spaceURL.substring(0, spaceURL.indexOf("/projects/") + 1);
                tempP = spaceURL.substring(0, spaceURL.endsWith("/") ? spaceURL.length() - 1 : spaceURL.length());
            }
            else if (!spaceURL.endsWith("/")) {
                tempS = spaceURL + '/';
            }
            else {
                tempS = spaceURL;
            }
        }
		this.spaceURL = tempS;
        this.projectURL = tempP;
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

		@Override
		public String getDisplayName() {
			return Messages.BacklogProjectProperty_DisplayName();
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
