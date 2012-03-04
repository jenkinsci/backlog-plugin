package hudson.plugins.backlog.webdav;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import hudson.FilePath;
import hudson.plugins.backlog.api.BaseTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class WebdavClientTest extends BaseTest {

	private static final String FILE_URL = BACKLOG_SPACE_URL + "dav/"
			+ BACKLOG_PROJECT_KEY + "/";

	private static final String TEST_PATH = "sardine/";

	private static final String TEST_TEXT_FILE = "test-put.txt";

	private static final String TEST_TEXT_STRING = "put test text.";

	private static WebdavClient client = new WebdavClient(FILE_URL,
			BACKLOG_USERNAME, BACKLOG_PASSWORD);

	private static FilePath filePath;

	private static FilePath basePath;

	static {
		try {
			File file = new File(WebdavClientTest.class.getResource(
					TEST_TEXT_FILE).toURI());
			File root = new File(WebdavClientTest.class.getResource("/")
					.toURI());

			filePath = new FilePath(file);
			basePath = new FilePath(root);
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	public static class NoSideEffectsTests {

		@After
		public void tearDown() {
			client.setRemovePrefixDirectory("");
		}

		@Test(expected = IllegalArgumentException.class)
		public void invalid() throws Exception {
			new WebdavClient(FILE_URL, "fail", "fail");
		}

		@Test
		public void getPathFromBase_notRemovePrefix() throws Exception {
			assertThat(client.getPathFromBase(filePath.getParent(), basePath),
					is("hudson/plugins/backlog/webdav/"));
		}

		@Test
		public void getPathFromBase_removePrefix() throws Exception {
			client.setRemovePrefixDirectory("/hudson/plugins/");

			assertThat(client.getPathFromBase(filePath.getParent(), basePath),
					is("backlog/webdav/"));
		}

		@Test(expected = IllegalArgumentException.class)
		public void getPathFromBase_notStartWithRemovePrefix() throws Exception {
			FilePath filePath = new FilePath(new File("base/path/file"));
			FilePath basePath = new FilePath(new File("base/"));
			client.setRemovePrefixDirectory("bad_prefix/");

			client.getPathFromBase(filePath, basePath);
		}

		@Test
		public void normalizeRemovePrefixDirectory() throws Exception {
			assertThat(client.normalizeRemovePrefixDirectory(""), is(""));

			assertThat(client.normalizeRemovePrefixDirectory("/hoge/fuge/"),
					is("hoge/fuge/"));

			assertThat(client.normalizeRemovePrefixDirectory("hoge/fuge/"),
					is("hoge/fuge/"));
		}

		@Test
		public void normalizeDirectory() throws Exception {
			assertThat(client.normalizeDirectory(""), is(""));

			assertThat(client.normalizeDirectory("/hoge/fuge"),
					is("/hoge/fuge/"));

			assertThat(client.normalizeDirectory("/hoge/fuge/"),
					is("/hoge/fuge/"));
		}

	}

	@Ignore("更新系APIは、普段のユニットテストでは実行しない")
	public static class SideEffectsTests {

		static {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(BACKLOG_USERNAME,
							BACKLOG_PASSWORD.toCharArray());
				}
			});
		}

		@Before
		public void setUp() throws Exception {
			client.createDirectory(TEST_PATH);
		}

		@After
		public void tearDown() throws Exception {
			client.delete(TEST_PATH);
		}

		@Test
		public void put() throws Exception {
			client.put(filePath, TEST_PATH);

			assertPutText(FILE_URL + TEST_PATH + TEST_TEXT_FILE);
		}

		@Test
		public void putWithParent() throws Exception {
			client.putWithParent(filePath, TEST_PATH, basePath);

			assertPutText(FILE_URL + TEST_PATH
					+ client.getPathFromBase(filePath, basePath));
		}

		private static void assertPutText(String url) throws Exception {
			BufferedReader reader = null;
			try {
				InputStream in = new URL(url).openStream();
				reader = new BufferedReader(new InputStreamReader(in));

				assertThat(reader.readLine(), is(TEST_TEXT_STRING));
			} finally {
				IOUtils.closeQuietly(reader);
			}

		}

	}

}
