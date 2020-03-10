package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import hudson.plugins.backlog.BacklogProjectProperty.DescriptorImpl;
import hudson.util.FormValidation;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class BacklogProjectPropertyTest extends HudsonTestCase {

	private BacklogProjectProperty bpp;

	@Test
	public final void testSpaceURLIsNull() {
		bpp = new BacklogProjectProperty(null, null, null, null);
		assertNull(bpp.url);
		assertNull(bpp.userId);
		assertTrue(StringUtils.isEmpty(bpp.getPassword().getPlainText()));
	}

	@Test
	public final void testSpaceURLIsProjectURL() {
		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test", "apiKey");
		assertEquals("https://demo.backlog.jp/projects/DORA", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword().getPlainText());
	}

	@Test
	public final void testSpaceURL() {
		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test", "apiKey");
		assertEquals("https://demo.backlog.jp/", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword().getPlainText());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp", "test",
				"test", "apiKey");
		assertEquals("https://demo.backlog.jp/", bpp.url);
		assertEquals("test", bpp.userId);
		assertEquals("test", bpp.getPassword().getPlainText());
	}

	@Test
	public void testGetProject() throws Exception {
		bpp = new BacklogProjectProperty(null, null, null, "apiKey");
		assertThat(bpp.getProject(), nullValue());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test", "apiKey");
		assertThat(bpp.getProject(), nullValue());

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test", "apiKey");
		assertThat(bpp.getProject(), is("DORA"));
	}

	@Test
	public void testGetSpaceURL() throws Exception {
		bpp = new BacklogProjectProperty(null, null, null, null);
		assertThat(bpp.getSpaceURL(), nullValue());

		bpp = new BacklogProjectProperty("https://demo.backlog.jp/", "test",
				"test", "apiKey");
		assertThat(bpp.getSpaceURL(), is("https://demo.backlog.jp/"));

		bpp = new BacklogProjectProperty(
				"https://demo.backlog.jp/projects/DORA", "test", "test", "apiKey");
		assertThat(bpp.getSpaceURL(), is("https://demo.backlog.jp/"));
	}

	@Test
	public void testDoCheckUserId() throws Exception {
		DescriptorImpl descriptor = new DescriptorImpl();

		assertThat(descriptor.doCheckUserId(""), is(FormValidation.ok()));
		assertThat(descriptor.doCheckUserId("ikikko_1234-github"), is(FormValidation.ok()));
		assertThat(descriptor.doCheckUserId("ikikko_1234-github@gmail.com"), is(FormValidation.ok()));

		assertThat(descriptor.doCheckUserId("ikikko+github@gmail.com"), is(not(FormValidation.ok())));
	}

}
