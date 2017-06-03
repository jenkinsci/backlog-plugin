package hudson.plugins.backlog.delegate;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.PullRequest;
import com.nulabinc.backlog4j.api.option.AddPullRequestCommentParams;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.backlog.BacklogProjectProperty;
import hudson.plugins.backlog.api.v2.BacklogClientFactory;
import hudson.plugins.backlog.pipeline.BacklogPullRequestSCMSource;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import jenkins.scm.api.SCMSource;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PullRequestDelegate {

    private final TaskListener listener;

    private final Run<?, ?> run;

    public PullRequestDelegate(TaskListener listener, Run run) {
        this.listener = listener;
        this.run = run;
    }

    public void notifyResult() throws InterruptedException, IOException {
        BacklogProjectProperty bpp = run.getParent().getProperty(BacklogProjectProperty.class);
        BacklogClient backlog;
        try {
            backlog = BacklogClientFactory.getBacklogClient(bpp);
        } catch (IllegalArgumentException e) {
            listener.getLogger().println(e.getMessage());
            return;
        }

        listener.getLogger().println("Adding pull request comments...");

        if (isBasedOnMultiBranchProject()) {
            commentBasedOnMultiBranchProject(bpp, backlog);
        } else {
            BuildData data = run.getAction(BuildData.class);
            if (data == null) {
                listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
                return;
            }

            commentBasedOnNonMultiBranchProject(bpp, data, backlog);
        }
    }

    private boolean isBasedOnMultiBranchProject() {
        if (run instanceof AbstractBuild) {
            return false;
        }
        if (run instanceof WorkflowRun) {
            return run.getParent().getParent() instanceof WorkflowMultiBranchProject;
        }

        listener.getLogger().println("Unknown project type : " + run.getParent().getClass());
        return false;
    }

    private void commentBasedOnMultiBranchProject(BacklogProjectProperty bpp, BacklogClient backlog) throws IOException {
        WorkflowJob workflowJob = ((WorkflowRun) run).getParent();

        for (SCMSource scmSource : ((WorkflowMultiBranchProject) workflowJob.getParent()).getSCMSources()) {
            if (!(scmSource instanceof BacklogPullRequestSCMSource)) {
                continue;
            }
            try {
                URIish uri = new URIish(((BacklogPullRequestSCMSource) scmSource).getRemote());
                long pullRequestId = Long.parseLong(workflowJob.getName());

                commentToPullRequest(bpp, backlog, uri, pullRequestId);
            } catch (URISyntaxException e) {
                listener.getLogger().println(e.getMessage());
                continue;
            }
        }
    }

    private void commentBasedOnNonMultiBranchProject(BacklogProjectProperty bpp, BuildData data, BacklogClient backlog) throws IOException {
        GitSCM scm = null;
        if (run instanceof AbstractBuild) {
            scm = (GitSCM) ((AbstractBuild) run).getProject().getScm();
        }
        if (run instanceof WorkflowRun) {
            WorkflowJob workflowJob = ((WorkflowRun) run).getParent();

            FlowDefinition definition = workflowJob.getDefinition();
            if (definition instanceof CpsScmFlowDefinition) {
                scm = (GitSCM) ((CpsScmFlowDefinition) definition).getScm();
            }
        }
        if (scm == null) {
            listener.getLogger().println("This project doesn't use Git as SCM. Can't comment a pull request.");
            return;
        }

        for (RemoteConfig repository : scm.getRepositories()) {
            Pattern pullRequestRefPattern;
            try {
                pullRequestRefPattern = getPullRequestRefPattern(repository);
            } catch (IllegalArgumentException e) {
                listener.getLogger().println(e.getMessage());
                continue;
            }

            for (URIish uri : repository.getURIs()) {
                for (Branch branch : data.getLastBuiltRevision().getBranches()) {
                    Matcher matcher = pullRequestRefPattern.matcher(branch.getName());
                    if (!matcher.matches()) {
                        continue;
                    }

                    long pullRequestId = Long.parseLong(matcher.group("number"));
                    commentToPullRequest(bpp, backlog, uri, pullRequestId);
                }
            }
        }
    }

    private void commentToPullRequest(BacklogProjectProperty bpp, BacklogClient backlog, URIish uri, long pullRequestId) throws IOException {
        PullRequest pullRequest = backlog.getPullRequest(bpp.getProject(), uri.getHumanishName(), pullRequestId);

        if (!pullRequest.getStatus().getStatus().equals(PullRequest.StatusType.Open)) {
            listener.getLogger().print("This pull request has been already closed : ");
            hyperlinkPullRequest(listener, bpp, uri, pullRequest);
            return;
        }

        Result result;
        if (inProgressPipeline(run)) {
            result = Result.SUCCESS;
        } else {
            result = run.getResult();
        }
        String content = String.format("%s Build %s ( %s )",
                convertEmoticonFromResult(result), result.toString(), run.getAbsoluteUrl());
        AddPullRequestCommentParams AddParams = new AddPullRequestCommentParams(
                bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber(), content);
        backlog.addPullRequestComment(AddParams);

        listener.getLogger().print("Added a pull request comment : ");
        hyperlinkPullRequest(listener, bpp, uri, pullRequest);
    }

    // refSpec     : +refs/pull/*:refs/remotes/origin/pr/*
    // destination : origin/pr
    //
    // result      : (refs/remotes/)?origin/pr/(\\d+)/head
    Pattern getPullRequestRefPattern(RemoteConfig repository) {
        RefSpec refSpec = repository.getFetchRefSpecs().get(0);

        String source = refSpec.getSource();
        if (!source.contains("refs/pull/")) {
            throw new IllegalArgumentException("Don't add pull request comments, because refspec's source '" + source + "' doesn't contain pull request refs 'refs/pull/'");
        }

        String destination = refSpec.getDestination().substring(Constants.R_REMOTES.length());
        destination = destination.replace("/*", "");

        return Pattern.compile(String.format("(refs/remotes/)?%s/(?<number>\\d+)/head", destination));
    }

    private void hyperlinkPullRequest(TaskListener listener, BacklogProjectProperty bpp, URIish uri, PullRequest pullRequest) throws IOException {
        String url = String.format("%sgit/%s/%s/pullRequests/%d",
                bpp.getSpaceURL(), bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber());
        String text = String.format("%s/%s#%d\n",
                bpp.getProject(), uri.getHumanishName(), pullRequest.getNumber());

        listener.hyperlink(url, text);
    }

    private boolean inProgressPipeline(Run run) {
        return run instanceof WorkflowRun && run.getResult() == null;
    }

    private String convertEmoticonFromResult(Result result) {
        if (result.isBetterOrEqualTo(Result.SUCCESS)) {
            return ":smiley:";
        } else if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
            return ":cry:";
        } else if (result.isBetterOrEqualTo(Result.FAILURE)) {
            return ":rage:";
        } else if (result.isBetterOrEqualTo(Result.NOT_BUILT)) {
            return ":mask:";
        } else {
            return ":astonished:";
        }
    }

}
