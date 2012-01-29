package hudson.plugins.backlog.webdav;

import hudson.plugins.backlog.api.BaseTest;

import java.io.File;

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
			File file = new File(this.getClass().getResource("test-put.txt")
					.toURI());
			client.put(file, TEST_PATH);
		}

	}

}
