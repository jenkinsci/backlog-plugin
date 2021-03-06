package hudson.plugins.backlog.pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.nulabinc.backlog4j.PullRequest;
import com.nulabinc.backlog4j.ResponseList;
import com.nulabinc.backlog4j.api.option.PullRequestQueryParams;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.model.queue.Tasks;
import hudson.plugins.backlog.BacklogProjectProperty;
import hudson.plugins.backlog.Messages;
import hudson.plugins.backlog.api.v2.BacklogClientFactory;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitTool;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import hudson.util.Secret;
import jenkins.model.Jenkins;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.*;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.jenkinsci.plugins.gitclient.FetchCommand;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Customize GitSCMSource to fit Backlog Git ( from git-plugin v3.0.4 )
 *
 * @see <a href="https://github.com/jenkinsci/git-plugin/blob/git-3.0.4/src/main/java/jenkins/plugins/git/AbstractGitSCMSource.java">AbstractGitSCMSource.java</a>
 * @see <a href="https://github.com/jenkinsci/git-plugin/blob/git-3.0.4/src/main/java/jenkins/plugins/git/GitSCMSource.java">GitSCMSource.java</a>
 * @author ikikko
 */
public class BacklogPullRequestSCMSource extends AbstractGitSCMSource {

    // --- Copy from GitSCMSource.java ---

    private static final String DEFAULT_INCLUDES = "*";

    private static final String DEFAULT_EXCLUDES = "";

    public static final Logger LOGGER = Logger.getLogger(BacklogPullRequestSCMSource.class.getName());

    private final String remote;

    private final String credentialsId;

    private final String includes;

    private final String excludes;

    private final boolean ignoreOnPushNotifications;

    private final String url;

    private final Secret apiKey;

    private final BacklogProjectProperty bpp;

    @CheckForNull
    private GitRepositoryBrowser browser;

    @CheckForNull
    private String gitTool;

    private List<GitSCMExtension> extensions;

    @DataBoundConstructor
    public BacklogPullRequestSCMSource(String id, String remote, String credentialsId, String includes, String excludes, boolean ignoreOnPushNotifications,
                                       String url, String apiKey) {
        super(id);
        this.remote = remote;
        this.credentialsId = credentialsId;
        this.includes = includes;
        this.excludes = excludes;
        this.ignoreOnPushNotifications = ignoreOnPushNotifications;

        this.url = url;
        this.apiKey = Secret.fromString(apiKey);

        this.bpp = new BacklogProjectProperty(url, "", "", apiKey);
    }

    public boolean isIgnoreOnPushNotifications() {
      return ignoreOnPushNotifications;
    }

    @Override
    public GitRepositoryBrowser getBrowser() {
        return browser;
    }

    // For Stapler only
    @Restricted(NoExternalUse.class)
    @DataBoundSetter
    public void setBrowser(GitRepositoryBrowser browser) {
        this.browser = browser;
    }

    @Override
    public String getGitTool() {
        return gitTool;
    }

    // For Stapler only
    @Restricted(NoExternalUse.class)
    @DataBoundSetter
    public void setGitTool(String gitTool) {
        this.gitTool = Util.fixEmptyAndTrim(gitTool);
    }

    @Override
    public List<GitSCMExtension> getExtensions() {
        if (extensions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<GitSCMExtension>(extensions));
    }

    // For Stapler only
    @Restricted(NoExternalUse.class)
    @DataBoundSetter
    public void setExtensions(List<GitSCMExtension> extensions) {
        this.extensions = Util.fixNull(extensions);
    }

    @Override
    public String getCredentialsId() {
        return credentialsId;
    }

    public String getRemote() {
        return remote;
    }

    @Override
    public String getIncludes() {
        return includes;
    }

    @Override
    public String getExcludes() {
        return excludes;
    }

    public String getUrl() {
        return url;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public BacklogProjectProperty getBpp() {
        return bpp;
    }

    @Override
    protected List<RefSpec> getRefSpecs() {
        // Change for pull request branches
        return Arrays.asList(new RefSpec("+refs/pull/*:refs/remotes/" + getRemoteName() + "/*"));
    }

    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.BacklogPullRequestSCMSource_DisplayName();
        }

        @SuppressFBWarnings(value="NP_NULL_PARAM_DEREF", justification="pending https://github.com/jenkinsci/credentials-plugin/pull/68")
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context,
                                                     @QueryParameter String remote,
                                                     @QueryParameter String credentialsId) {
            if (context == null && !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER) ||
                context != null && !context.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            context instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task)context) : ACL.SYSTEM,
                            context,
                            StandardUsernameCredentials.class,
                            URIRequirementBuilder.fromUri(remote).build(),
                            GitClient.CREDENTIALS_MATCHER)
                    .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context,
                                                   @QueryParameter String url,
                                                   @QueryParameter String value) {
            if (context == null && !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER) ||
                context != null && !context.hasPermission(Item.EXTENDED_READ)) {
                return FormValidation.ok();
            }

            value = Util.fixEmptyAndTrim(value);
            if (value == null) {
                return FormValidation.ok();
            }

            url = Util.fixEmptyAndTrim(url);
            if (url == null)
            // not set, can't check
            {
                return FormValidation.ok();
            }

            for (ListBoxModel.Option o : CredentialsProvider.listCredentials(
                    StandardUsernameCredentials.class,
                    context,
                    context instanceof Queue.Task
                            ? Tasks.getAuthenticationOf((Queue.Task) context)
                            : ACL.SYSTEM,
                    URIRequirementBuilder.fromUri(url).build(),
                    GitClient.CREDENTIALS_MATCHER)) {
                if (StringUtils.equals(value, o.value)) {
                    // TODO check if this type of credential is acceptable to the Git client or does it merit warning
                    // NOTE: we would need to actually lookup the credential to do the check, which may require
                    // fetching the actual credential instance from a remote credentials store. Perhaps this is
                    // not required
                    return FormValidation.ok();
                }
            }
            // no credentials available, can't check
            return FormValidation.warning("Cannot find any credentials with id " + value);
        }

        public GitSCM.DescriptorImpl getSCMDescriptor() {
            return (GitSCM.DescriptorImpl)Jenkins.getInstance().getDescriptor(GitSCM.class);
        }

        public List<GitSCMExtensionDescriptor> getExtensionDescriptors() {
            return getSCMDescriptor().getExtensionDescriptors();
        }

        public List<Descriptor<RepositoryBrowser<?>>> getBrowserDescriptors() {
            return getSCMDescriptor().getBrowserDescriptors();
        }

        public boolean showGitToolOptions() {
            return getSCMDescriptor().showGitToolOptions();
        }

        public ListBoxModel doFillGitToolItems() {
            return getSCMDescriptor().doFillGitToolItems();
        }
    }

    @Extension
    public static class ListenerImpl extends GitStatus.Listener {

        @Override
        public List<GitStatus.ResponseContributor> onNotifyCommit(URIish uri, final String sha1, List<ParameterValue> buildParameters, String... branches) {
            List<GitStatus.ResponseContributor> result = new ArrayList<GitStatus.ResponseContributor>();
            final boolean notified[] = {false};
            // run in high privilege to see all the projects anonymous users don't see.
            // this is safe because when we actually schedule a build, it's a build that can
            // happen at some random time anyway.
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                LOGGER.severe("Jenkins instance is null in BacklogPullRequestSCMSource.onNotifyCommit");
                return result;
            }
            SecurityContext old = jenkins.getACL().impersonate(ACL.SYSTEM);
            try {
                if (branches.length > 0) {
                    final URIish u = uri;
                    for (final String branch: branches) {
                        SCMHeadEvent.fireNow(new SCMHeadEvent<String>(SCMEvent.Type.UPDATED, branch){
                            @Override
                            public boolean isMatch(@NonNull SCMNavigator navigator) {
                                return false;
                            }

                            @NonNull
                            @Override
                            public String getSourceName() {
                                // we will never be called here as do not match any navigator
                                return u.getHumanishName();
                            }

                            @Override
                            public boolean isMatch(SCMSource source) {
                                if (source instanceof BacklogPullRequestSCMSource) {
                                    BacklogPullRequestSCMSource git = (BacklogPullRequestSCMSource) source;
                                    if (git.ignoreOnPushNotifications) {
                                        return false;
                                    }
                                    URIish remote;
                                    try {
                                        remote = new URIish(git.getRemote());
                                    } catch (URISyntaxException e) {
                                        // ignore
                                        return false;
                                    }
                                    if (GitStatus.looselyMatches(u, remote)) {
                                        notified[0] = true;
                                        return true;
                                    }
                                    return false;
                                }
                                return false;
                            }

                            @NonNull
                            @Override
                            public Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source) {
                                SCMHead head = new SCMHead(branch);
                                return Collections.<SCMHead, SCMRevision>singletonMap(head,
                                        sha1 != null ? new SCMRevisionImpl(head, sha1) : null);
                            }

                            @Override
                            public boolean isMatch(@NonNull SCM scm) {
                                return false; // TODO rewrite the legacy event system to fire through SCM API
                            }
                        });
                    }
                } else {
                    for (final SCMSourceOwner owner : SCMSourceOwners.all()) {
                        for (SCMSource source : owner.getSCMSources()) {
                            if (source instanceof BacklogPullRequestSCMSource) {
                                BacklogPullRequestSCMSource git = (BacklogPullRequestSCMSource) source;
                                if (git.ignoreOnPushNotifications) {
                                    continue;
                                }
                                URIish remote;
                                try {
                                    remote = new URIish(git.getRemote());
                                } catch (URISyntaxException e) {
                                    // ignore
                                    continue;
                                }
                                if (GitStatus.looselyMatches(uri, remote)) {
                                    LOGGER.info("Triggering the indexing of " + owner.getFullDisplayName());
                                    owner.onSCMSourceUpdated(source);
                                    result.add(new GitStatus.ResponseContributor() {
                                        @Override
                                        public void addHeaders(StaplerRequest req, StaplerResponse rsp) {
                                            rsp.addHeader("Triggered", owner.getAbsoluteUrl());
                                        }

                                        @Override
                                        public void writeBody(PrintWriter w) {
                                            w.println("Scheduled indexing of " + owner.getFullDisplayName());
                                        }
                                    });
                                    notified[0] = true;
                                }
                            }
                        }
                    }
                }
            } finally {
                SecurityContextHolder.setContext(old);
            }
            if (!notified[0]) {
                result.add(new GitStatus.MessageResponseContributor("No Git consumers using SCM API plugin for: " + uri.toString()));
            }
            return result;
        }
    }

    // --- Copy from AbstractGitSCMSource.java  ---

    @CheckForNull
    @Override
    protected SCMRevision retrieve(@NonNull final SCMHead head, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        return doRetrieve(new Retriever<SCMRevision>() {
            @Override
            public SCMRevision run(GitClient client, String remoteName) throws IOException, InterruptedException {
                for (Branch b : client.getRemoteBranches()) {
                    // Change for pull request branches
                    // String branchName = StringUtils.removeStart(b.getName(), remoteName + "/");
                    String branchName = StringUtils.removeEnd(StringUtils.removeStart(b.getName(), remoteName + "/"), "/head");
                    if (branchName.equals(head.getName())) {
                        return new SCMRevisionImpl(head, b.getSHA1String());
                    }
                }
                return null;
            }
        }, listener, /* we don't prune remotes here, as we just want one head's revision */false);
    }

    @Override
    protected void retrieve(@CheckForNull final SCMSourceCriteria criteria,
                            @NonNull final SCMHeadObserver observer,
                            @CheckForNull final SCMHeadEvent<?> event,
                            @NonNull final TaskListener listener)
            throws IOException, InterruptedException {
        doRetrieve(new Retriever<Void>() {
            @Override
            public Void run(GitClient client, String remoteName) throws IOException, InterruptedException {
                final Repository repository = client.getRepository();
                listener.getLogger().println("Getting remote branches...");
                // Change for pull request branches
                ResponseList<PullRequest> pullRequests = getOpenPullRequests();
                try (RevWalk walk = new RevWalk(repository)) {
                    walk.setRetainBody(false);
                    for (Branch b : client.getRemoteBranches()) {
                        checkInterrupt();
                        if (!b.getName().startsWith(remoteName + "/")) {
                            continue;
                        }
                        // Change for pull request branches
                        // final String branchName = StringUtils.removeStart(b.getName(), remoteName + "/");
                        final String branchName = StringUtils.removeEnd(StringUtils.removeStart(b.getName(), remoteName + "/"), "/head");
                        listener.getLogger().println("Checking branch " + branchName);
                        if (isExcluded(branchName)){
                            continue;
                        }
                        // Change for pull request branches
                        if (!isPullRequestOpen(pullRequests, branchName)) {
                            continue;
                        }
                        if (criteria != null) {
                            RevCommit commit = walk.parseCommit(b.getSHA1());
                            final long lastModified = TimeUnit.SECONDS.toMillis(commit.getCommitTime());
                            final RevTree tree = commit.getTree();
                            SCMSourceCriteria.Probe probe = new SCMProbe() {
                                @Override
                                public void close() throws IOException {
                                    // no-op
                                }

                                @Override
                                public String name() {
                                    return branchName;
                                }

                                @Override
                                public long lastModified() {
                                    return lastModified;
                                }

                                @Override
                                @NonNull
                                @SuppressFBWarnings(value = "NP_LOAD_OF_KNOWN_NULL_VALUE",
                                                    justification = "TreeWalk.forPath can return null, compiler "
                                                            + "generated code for try with resources handles it")
                                public SCMProbeStat stat(@NonNull String path) throws IOException {
                                    try (TreeWalk tw = TreeWalk.forPath(repository, path, tree)) {
                                        if (tw == null) {
                                            return SCMProbeStat.fromType(SCMFile.Type.NONEXISTENT);
                                        }
                                        FileMode fileMode = tw.getFileMode(0);
                                        if (fileMode == FileMode.MISSING) {
                                            return SCMProbeStat.fromType(SCMFile.Type.NONEXISTENT);
                                        }
                                        if (fileMode == FileMode.EXECUTABLE_FILE) {
                                            return SCMProbeStat.fromType(SCMFile.Type.REGULAR_FILE);
                                        }
                                        if (fileMode == FileMode.REGULAR_FILE) {
                                            return SCMProbeStat.fromType(SCMFile.Type.REGULAR_FILE);
                                        }
                                        if (fileMode == FileMode.SYMLINK) {
                                            return SCMProbeStat.fromType(SCMFile.Type.LINK);
                                        }
                                        if (fileMode == FileMode.TREE) {
                                            return SCMProbeStat.fromType(SCMFile.Type.DIRECTORY);
                                        }
                                        return SCMProbeStat.fromType(SCMFile.Type.OTHER);
                                    }
                                }
                            };
                            if (criteria.isHead(probe, listener)) {
                                listener.getLogger().println("Met criteria");
                            } else {
                                listener.getLogger().println("Does not meet criteria");
                                continue;
                            }
                        }
                        SCMHead head = new SCMHead(branchName);
                        SCMRevision hash = new SCMRevisionImpl(head, b.getSHA1String());
                        observer.observe(head, hash);
                        if (!observer.isObserving()) {
                            return null;
                        }
                    }
                }

                listener.getLogger().println("Done.");
                return null;
            }
        }, listener, true);
    }

    private interface Retriever<T> {
        T run(GitClient client, String remoteName) throws IOException, InterruptedException;
    }

    private <T> T doRetrieve(BacklogPullRequestSCMSource.Retriever<T> retriever, @NonNull TaskListener listener, boolean prune)
            throws IOException, InterruptedException {
        String cacheEntry = getCacheEntry();
        Lock cacheLock = getCacheLock(cacheEntry);
        cacheLock.lock();
        try {
            File cacheDir = getCacheDir(cacheEntry);
            Git git = Git.with(listener, new EnvVars(EnvVars.masterEnvVars)).in(cacheDir);
            GitTool tool = resolveGitTool();
            if (tool != null) {
                git.using(tool.getGitExe());
            }
            GitClient client = git.getClient();
            client.addDefaultCredentials(getCredentials());
            if (!client.hasGitRepo()) {
                listener.getLogger().println("Creating git repository in " + cacheDir);
                client.init();
            }
            String remoteName = getRemoteName();
            listener.getLogger().println("Setting " + remoteName + " to " + getRemote());
            client.setRemoteUrl(remoteName, getRemote());
            listener.getLogger().println((prune ? "Fetching & pruning " : "Fetching ") + remoteName + "...");
            FetchCommand fetch = client.fetch_();
            if (prune) {
                fetch = fetch.prune();
            }
            URIish remoteURI = null;
            try {
                remoteURI = new URIish(remoteName);
            } catch (URISyntaxException ex) {
                listener.getLogger().println("URI syntax exception for '" + remoteName + "' " + ex);
            }
            fetch.from(remoteURI, getRefSpecs()).execute();
            return retriever.run(client, remoteName);
        } finally {
            cacheLock.unlock();
        }
    }

    private ResponseList<PullRequest> getOpenPullRequests() throws MalformedURLException {
        PullRequestQueryParams params = new PullRequestQueryParams();
        params.statusType(Collections.singletonList(PullRequest.StatusType.Open));
        params.count(100); // max count

        String repoName = new URIish(new URL(remote)).getHumanishName();
        return BacklogClientFactory.getBacklogClient(bpp).getPullRequests(bpp.getProject(), repoName, params);
    }

    private boolean isPullRequestOpen(ResponseList<PullRequest> pullRequests, String branchName) {
        for (PullRequest pullRequest : pullRequests) {
            if (pullRequest.getNumber() == Long.parseLong(branchName)) {
                return true;
            }
        }
        return false;
    }

}
