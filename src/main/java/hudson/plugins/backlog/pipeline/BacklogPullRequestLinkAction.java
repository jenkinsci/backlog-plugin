package hudson.plugins.backlog.pipeline;

import com.nulabinc.backlog4j.PullRequest;
import hudson.model.Action;
import hudson.plugins.backlog.BacklogProjectProperty;
import org.eclipse.jgit.transport.URIish;

import java.net.MalformedURLException;
import java.net.URL;

public class BacklogPullRequestLinkAction implements Action {

    private final BacklogPullRequestSCMSource source;
    private final PullRequest pullRequest;

    public BacklogPullRequestLinkAction(BacklogPullRequestSCMSource source, PullRequest pullRequest) {
        this.source = source;
        this.pullRequest = pullRequest;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/backlog/icon.png";
    }

    @Override
    public String getDisplayName() {
        return "Backlog";
    }

    @Override
    public String getUrlName() {
        try {
            BacklogProjectProperty bpp = source.getBpp();
            String repoName = new URIish(new URL(source.getRemote())).getHumanishName();

            return String.format("%sgit/%s/%s/pullRequests/%d",
                    bpp.getSpaceURL(), bpp.getProject(), repoName, pullRequest.getNumber());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
