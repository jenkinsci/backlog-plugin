package hudson.plugins.backlog.webdav;

import hudson.plugins.backlog.api.BaseTest;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class WebdavClientTest extends BaseTest {

	private static final String FILE_URL = BACKLOG_SPACE_URL + "dav/"
			+ BACKLOG_PROJECT_KEY + "/";

	private WebdavClient client;

	@Before
	public void setUp() {
		client = new WebdavClient(FILE_URL, BACKLOG_USERNAME, BACKLOG_PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid() throws Exception {
		client = new WebdavClient(FILE_URL, "fail", "fail");
	}

	// @Ignore("更新系APIは、普段のユニットテストでは実行しない")
	@Test
	public void put() throws Exception {
		File file = new File(this.getClass().getResource("test-put.txt")
				.toURI());

		client.put(file, "sardine/");
	}

}
