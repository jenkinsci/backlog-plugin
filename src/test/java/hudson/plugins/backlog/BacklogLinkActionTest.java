package hudson.plugins.backlog;

import java.util.regex.Matcher;

import hudson.MarkupText;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BacklogLinkActionTest {

	@Test
	public final void testGetUrlName() {
        BacklogProjectProperty prop = new BacklogProjectProperty("https://demo.backlog.jp");
        BacklogLinkAction action = new BacklogLinkAction(prop);
        assertEquals("https://demo.backlog.jp/", action.getUrlName());
	}

	@Test
	public final void testGetUrlNameProject() {
        BacklogProjectProperty prop = new BacklogProjectProperty("https://demo.backlog.jp/projects/DORA");
        BacklogLinkAction action = new BacklogLinkAction(prop);
        assertEquals("https://demo.backlog.jp/projects/DORA", action.getUrlName());
	}
}
