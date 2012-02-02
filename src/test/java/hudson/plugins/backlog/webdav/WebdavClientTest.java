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
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
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

	public static class NoSideEffectsTests {

		@Test(expected = IllegalArgumentException.class)
		public void invalid() throws Exception {
			new WebdavClient(FILE_URL, "fail", "fail");
		}

	}

	// @Ignore("更新系APIは、普段のユニットテストでは実行しない")
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
			File file = new File(this.getClass().getResource(TEST_TEXT_FILE)
					.toURI());
			FilePath filePath = new FilePath(file);

			client.put(filePath, TEST_PATH);

			assertPutText(FILE_URL + TEST_PATH + TEST_TEXT_FILE);
		}

		@Test
		public void putWithParent() throws Exception {
			File file = new File(this.getClass().getResource(TEST_TEXT_FILE)
					.toURI());
			File root = new File(this.getClass().getResource("/").toURI());
			FilePath filePath = new FilePath(file);
			FilePath rootPath = new FilePath(root);

			client.putWithParent(filePath, TEST_PATH, rootPath);

			assertPutText(FILE_URL + TEST_PATH
					+ client.getPathFromBase(filePath, rootPath));
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
