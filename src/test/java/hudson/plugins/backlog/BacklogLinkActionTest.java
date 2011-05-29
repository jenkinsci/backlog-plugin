package hudson.plugins.backlog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BacklogLinkActionTest {

	@Test
	public final void testGetUrlName() {
		BacklogProjectProperty prop = new BacklogProjectProperty(
				"https://demo.backlog.jp", "test", "test");
		BacklogLinkAction action = new BacklogLinkAction(prop);
		assertEquals("https://demo.backlog.jp/", action.getUrlName());
	}

	@Test
	public final void testGetUrlNameProject() {
		BacklogProjectProperty prop = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		BacklogLinkAction action = new BacklogLinkAction(prop);
		assertEquals("https://demo.backlog.jp/projects/DORA",
				action.getUrlName());
	}
}
