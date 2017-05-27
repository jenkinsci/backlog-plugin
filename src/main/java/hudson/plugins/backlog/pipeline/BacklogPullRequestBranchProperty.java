package hudson.plugins.backlog.pipeline;

import com.nulabinc.backlog4j.BacklogAPIException;
import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.PullRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.backlog.BacklogProjectProperty;
import hudson.plugins.backlog.Messages;
import hudson.plugins.backlog.api.v2.BacklogClientFactory;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;
import jenkins.branch.MultiBranchProject;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Additional information associated with {@link BacklogPullRequestSCMSource}
 *
 * <ul>
 *     <li>Change display name</li>
 *     <li>Add a link for pull request</li>
 * </ul>
 */
public class BacklogPullRequestBranchProperty extends BranchProperty {

    private static final Logger LOGGER = Logger.getLogger(BacklogPullRequestBranchProperty.class.getName());

    @DataBoundConstructor
    public BacklogPullRequestBranchProperty() {}

    @Override
    public <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return new JobDecorator<P, B>() {
            @NonNull
            @Override
            public P project(@NonNull P project) {
                MultiBranchProject parent = (MultiBranchProject) project.getParent();

                try {
                    Object source = parent.getSCMSources().get(0);
                    if (!(source instanceof BacklogPullRequestSCMSource)) {
                        return project;
                    }
                    BacklogPullRequestSCMSource backlogSource = (BacklogPullRequestSCMSource) source;

                    BacklogProjectProperty bpp = backlogSource.getBpp();
                    BacklogClient backlog = BacklogClientFactory.getBacklogClient(bpp);
                    String repoName = new URIish(new URL(backlogSource.getRemote())).getHumanishName();
                    Long number = Long.parseLong(project.getName());
                    PullRequest pullRequest = backlog.getPullRequest(bpp.getProject(), repoName, number);

                    project.setDisplayName("#" + pullRequest.getNumber() + " (" + pullRequest.getSummary() + ")");
                    project.replaceAction(new BacklogPullRequestLinkAction(backlogSource, pullRequest));
                } catch (IOException | IllegalArgumentException | BacklogAPIException e) {
                    LOGGER.log(Level.WARNING, "Failed to apply Backlog multibranch project to " + project.getFullName(), e);
                }

                return project;
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.BacklogPullRequestBranchProperty_DisplayName();
        }
    }



}
