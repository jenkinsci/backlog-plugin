package hudson.plugins.backlog;

import com.nulabinc.backlog4j.*;
import com.nulabinc.backlog4j.api.option.PullRequestQueryParams;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.Arrays;

public class BacklogLinkActionTest extends HudsonTestCase {

	@Test
	public final void testGetUrlName() {
		BacklogProjectProperty prop = new BacklogProjectProperty(
				"https://demo.backlog.jp", "test", "test", "apiKey");
		BacklogLinkAction action = new BacklogLinkAction(prop);
		assertEquals("https://demo.backlog.jp/", action.getUrlName());
	}

	@Test
	public final void testGetUrlNameProject() {
		BacklogProjectProperty prop = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test", "apiKey");
		BacklogLinkAction action = new BacklogLinkAction(prop);
		assertEquals("https://demo.backlog.jp/projects/DORA",
				action.getUrlName());
	}
}
