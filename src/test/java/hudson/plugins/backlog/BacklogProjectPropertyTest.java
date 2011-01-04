package hudson.plugins.backlog;

import java.util.regex.Matcher;

import hudson.MarkupText;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BacklogProjectPropertyTest {

    private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
        bpp = new BacklogProjectProperty(null);
        assertNull(bpp.spaceURL);
        assertNull(bpp.projectURL);
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
        bpp = new BacklogProjectProperty("https://demo.backlog.jp/projects/DORA");
        assertNotNull(bpp.spaceURL);
        assertNotNull(bpp.projectURL);
        assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
        assertEquals("https://demo.backlog.jp/projects/DORA", bpp.projectURL);

        bpp = new BacklogProjectProperty("https://demo.backlog.jp/projects/FOO/");
        assertNotNull(bpp.spaceURL);
        assertNotNull(bpp.projectURL);
        assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
        assertEquals("https://demo.backlog.jp/projects/FOO", bpp.projectURL);
	}

	@Test
	public final void testSpaceURL() {
        bpp = new BacklogProjectProperty("https://demo.backlog.jp/");
        assertNotNull(bpp.spaceURL);
        assertNull(bpp.projectURL);
        assertEquals("https://demo.backlog.jp/", bpp.spaceURL);

        bpp = new BacklogProjectProperty("https://demo.backlog.jp");
        assertNotNull(bpp.spaceURL);
        assertNull(bpp.projectURL);
        assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
	}
}
