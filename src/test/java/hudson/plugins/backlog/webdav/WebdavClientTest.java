package hudson.plugins.backlog.webdav;

import hudson.FilePath;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WebdavClientTest {

	private static final String TEST_PATH = "sardine/";

	private static final String TEST_TEXT_FILE = "test-put.txt";

	private static final String TEST_TEXT_STRING = "put test text.";

	private static WebdavClient client = new WebdavClient();

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

	@After
	public void tearDown() {
		client.setRemovePrefix("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid() throws Exception {
		new WebdavClient("dummy", "fail", "fail");
	}

	@Test
	public void getPathFromBase_notRemovePrefix() throws Exception {
		assertThat(client.getPathFromBase(filePath.getParent(), basePath),
				is("hudson/plugins/backlog/webdav/"));
	}

	@Test
	public void getPathFromBase_removePrefix() throws Exception {
		client.setRemovePrefix("/hudson/plugins/");

		assertThat(client.getPathFromBase(filePath.getParent(), basePath),
				is("backlog/webdav/"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPathFromBase_notStartWithRemovePrefix() throws Exception {
		FilePath filePath = new FilePath(new File("base/path/file"));
		FilePath basePath = new FilePath(new File("base/"));
		client.setRemovePrefix("bad_prefix/");

		client.getPathFromBase(filePath, basePath);
	}

	@Test
	public void normalizeDirectory() throws Exception {
		String expected = "hoge/fuge/";

		assertThat(client.normalizeDirectory(""), is(""));

		assertThat(client.normalizeDirectory("/hoge/fuge/"), is(expected));

		assertThat(client.normalizeDirectory("/hoge/fuge"), is(expected));

		assertThat(client.normalizeDirectory("hoge/fuge/"), is(expected));

		assertThat(client.normalizeDirectory("hoge/fuge"), is(expected));
	}

}
