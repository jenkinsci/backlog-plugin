package hudson.plugins.backlog.pipeline;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.backlog.Messages;
import hudson.plugins.backlog.delegate.PullRequestDelegate;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.inject.Inject;

public class BacklogPullRequestStep extends AbstractStepImpl {

    @DataBoundConstructor
    public BacklogPullRequestStep() {
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(BacklogPullRequestSendStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "backlogPullRequest";
        }

        @Override
        public String getDisplayName() {
            return Messages.BacklogPullRequestNotifier_DisplayName();
        }

    }

    public static class BacklogPullRequestSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient BacklogPullRequestStep step;

        @StepContextParameter
        transient TaskListener listener;

        @StepContextParameter
        transient Run run;

        @Override
        protected Void run() throws Exception {
            new PullRequestDelegate(listener, run).notifyResult();
            return null;
        }

    }

}
