package hudson.plugins.backlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BacklogProjectPropertyTest {

	private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
		bpp = new BacklogProjectProperty(null);
		assertNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNull(bpp.getProject());
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA");
		assertNotNull(bpp.spaceURL);
		assertNotNull(bpp.projectURL);
		assertNotNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("https://demo.backlog.jp/projects/DORA", bpp.projectURL);
		assertEquals("DORA", bpp.getProject());

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/FOO/");
		assertNotNull(bpp.spaceURL);
		assertNotNull(bpp.projectURL);
		assertNotNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("https://demo.backlog.jp/projects/FOO", bpp.projectURL);
		assertEquals("FOO", bpp.getProject());
	}

	@Test
	public final void testSpaceURL() {
		bpp = new BacklogProjectProperty("https://demo.backlog.jp/");
		assertNotNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);

		bpp = new BacklogProjectProperty("https://demo.backlog.jp");
		assertNotNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
	}
}
