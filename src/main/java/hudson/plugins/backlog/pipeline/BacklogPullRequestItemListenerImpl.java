package hudson.plugins.backlog.pipeline;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.PullRequest;
import hudson.BulkChange;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.listeners.ItemListener;
import hudson.plugins.backlog.BacklogProjectProperty;
import hudson.plugins.backlog.api.v2.BacklogClientFactory;
import jenkins.scm.api.SCMSource;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see <a href="https://github.com/jenkinsci/github-organization-folder-plugin/blob/master/src/main/java/org/jenkinsci/plugins/orgfolder/github/MainLogic.java">GitHub Organization Folder Plugin</a>
 */
@Extension
public class BacklogPullRequestItemListenerImpl extends ItemListener {

    @Override
    public void onCreated(Item item) {
        maybeApply(item);
    }

    @Override
    public void onUpdated(Item item) {
        maybeApply(item);
    }

    private void maybeApply(Item item) {
        if (!(item instanceof WorkflowJob)) {
            return;
        }

        if (!(item.getParent() instanceof WorkflowMultiBranchProject)) {
            return;
        }

        WorkflowJob job = (WorkflowJob) item;
        WorkflowMultiBranchProject project = (WorkflowMultiBranchProject) item.getParent();

        if (UPDATING.get().add(job)) {
            BulkChange bc = new BulkChange(item);

            try {
                SCMSource source = project.getSCMSources().get(0);
                if (!(source instanceof BacklogPullRequestSCMSource)) {
                    return;
                }
                BacklogPullRequestSCMSource backlogSource = (BacklogPullRequestSCMSource) source;

                BacklogProjectProperty bpp = backlogSource.getBpp();
                BacklogClient backlog = BacklogClientFactory.getBacklogClient(bpp);
                String repoName = new URIish(new URL(backlogSource.getRemote())).getHumanishName();
                Long number = Long.parseLong(item.getName());
                PullRequest pullRequest = backlog.getPullRequest(bpp.getProject(), repoName, number);

                job.setDisplayName("#" + pullRequest.getNumber() + " (" + pullRequest.getSummary() + ")");
                job.replaceAction(new BacklogPullRequestLinkAction(backlogSource, pullRequest));

                bc.commit();
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Failed to apply Backlog multibranch project to " + item.getFullName(), e);
            } finally {
                bc.abort();
                UPDATING.get().remove(job);
            }
        }

    }

    private final ThreadLocal<Set<Item>> UPDATING = new ThreadLocal<Set<Item>>() {
        @Override
        protected Set<Item> initialValue() {
            return new HashSet<Item>();
        }
    };

    private static final Logger LOGGER = Logger.getLogger(BacklogPullRequestItemListenerImpl.class.getName());

}
