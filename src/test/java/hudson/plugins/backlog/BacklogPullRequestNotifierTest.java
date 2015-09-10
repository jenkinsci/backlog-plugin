package hudson.plugins.backlog;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BacklogPullRequestNotifierTest {

    BacklogPullRequestNotifier notifier = new BacklogPullRequestNotifier();

    @Test
    public void getPullRequestRefPattern() throws Exception {
        RemoteConfig repository = setUpRepository("+refs/pull/*:refs/remotes/origin/pr/*");

        Pattern patttern = notifier.getPullRequestRefPattern(repository);

        Matcher matcher = patttern.matcher("origin/pr/12/head");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group("number"), is("12"));

        matcher = patttern.matcher("refs/remotes/origin/pr/12/head");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group("number"), is("12"));

        matcher = patttern.matcher("origin/master");
        assertThat(matcher.matches(), is(false));

        matcher = patttern.matcher("HEAD");
        assertThat(matcher.matches(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPullRequestRefPattern_notPullRequestRef() throws Exception {
        RemoteConfig repository = setUpRepository("+refs/heads/*:refs/remotes/origin/*"); // default refspec

        notifier.getPullRequestRefPattern(repository);
    }

    private RemoteConfig setUpRepository(String refSpec) throws URISyntaxException {
        RemoteConfig repository = new RemoteConfig(new Config(), "origin");

        List<RefSpec> specs = new ArrayList<RefSpec>(Arrays.asList(new RefSpec(refSpec)));
        repository.setFetchRefSpecs(specs);

        return repository;
    }

}