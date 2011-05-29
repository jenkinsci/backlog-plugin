package hudson.plugins.backlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BacklogProjectPropertyTest {

	private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
		bpp = new BacklogProjectProperty(null, null, null);
		assertNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNull(bpp.userId);
		assertNull(bpp.password);
		assertNull(bpp.getProject());
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertNotNull(bpp.spaceURL);
		assertNotNull(bpp.projectURL);
		assertNotNull(bpp.userId);
		assertNotNull(bpp.password);
		assertNotNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("https://demo.backlog.jp/projects/DORA", bpp.projectURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);
		assertEquals("DORA", bpp.getProject());

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/FOO/", "test", "test");
		assertNotNull(bpp.spaceURL);
		assertNotNull(bpp.projectURL);
		assertNotNull(bpp.userId);
		assertNotNull(bpp.password);
		assertNotNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("https://demo.backlog.jp/projects/FOO", bpp.projectURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);
		assertEquals("FOO", bpp.getProject());
	}

	@Test
	public final void testSpaceURL() {
		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertNotNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);

		bpp = new BacklogProjectProperty("https://demo.backlog.jp", "test",
				"test");
		assertNotNull(bpp.spaceURL);
		assertNull(bpp.projectURL);
		assertNotNull(bpp.userId);
		assertNotNull(bpp.password);
		assertNull(bpp.getProject());
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);
	}
}
