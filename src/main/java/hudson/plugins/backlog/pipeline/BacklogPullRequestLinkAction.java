package hudson.plugins.backlog.pipeline;

import com.nulabinc.backlog4j.PullRequest;
import hudson.plugins.backlog.BacklogLinkAction;
import org.eclipse.jgit.transport.URIish;

import java.net.MalformedURLException;
import java.net.URL;

public class BacklogPullRequestLinkAction extends BacklogLinkAction {

    private final BacklogPullRequestSCMSource source;
    private final PullRequest pullRequest;

    public BacklogPullRequestLinkAction(BacklogPullRequestSCMSource source, PullRequest pullRequest) {
        super(source.getBpp());

        this.source = source;
        this.pullRequest = pullRequest;
    }

    @Override
    public String getUrlName() {
        try {
            String repoName = new URIish(new URL(source.getRemote())).getHumanishName();

            return String.format("%sgit/%s/%s/pullRequests/%d",
                    prop.getSpaceURL(), prop.getProject(), repoName, pullRequest.getNumber());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
