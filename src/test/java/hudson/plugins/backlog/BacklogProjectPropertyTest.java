package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class BacklogProjectPropertyTest extends HudsonTestCase {

	private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
		bpp = new BacklogProjectProperty(null, null, null);
		assertNull(bpp.url);
		assertNull(bpp.userId);
		assertTrue(StringUtils.isEmpty(bpp.getPassword()));
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertEquals("https://demo.backlog.jp/projects/DORA", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword());
	}

	@Test
	public final void testSpaceURL() {
		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertEquals("https://demo.backlog.jp/", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp", "test",
				"test");
		assertEquals("https://demo.backlog.jp/", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword());
	}

	@Test
	public void testGetProject() throws Exception {
		bpp = new BacklogProjectProperty(null, null, null);
		assertThat(bpp.getProject(), nullValue());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertThat(bpp.getProject(), nullValue());

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertThat(bpp.getProject(), is("DORA"));
	}

	@Test
	public void testGetSpaceURL() throws Exception {
		bpp = new BacklogProjectProperty(null, null, null);
		assertThat(bpp.getSpaceURL(), nullValue());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertThat(bpp.getSpaceURL(), is("https://demo.backlog.jp/"));

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertThat(bpp.getSpaceURL(), is("https://demo.backlog.jp/"));
	}

}
