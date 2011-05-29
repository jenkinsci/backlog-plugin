package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BacklogProjectPropertyTest {

	private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
		bpp = new BacklogProjectProperty(null, null, null);
		assertNull(bpp.spaceURL);
		assertNull(bpp.userId);
		assertNull(bpp.password);
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertEquals("https://demo.backlog.jp/projects/DORA", bpp.spaceURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);
	}

	@Test
	public final void testSpaceURL() {
		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);

		bpp = new BacklogProjectProperty("https://demo.backlog.jp", "test",
				"test");
		assertEquals("https://demo.backlog.jp/", bpp.spaceURL);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.password);
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
		assertThat(bpp.getSpaceURL2(), nullValue());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test");
		assertThat(bpp.getSpaceURL2(), is("https://demo.backlog.jp/"));

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test");
		assertThat(bpp.getSpaceURL2(), is("https://demo.backlog.jp/"));
	}

}
