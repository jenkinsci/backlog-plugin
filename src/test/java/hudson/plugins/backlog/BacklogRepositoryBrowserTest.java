package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class BacklogRepositoryBrowserTest {

	@Test
	public void testGetSpaceURLAndProjectWhenOptionUrlset() throws Exception {
		BacklogRepositoryBrowser browser = new BacklogRepositoryBrowser(
				"https://demo.backlog.jp/svn/STWK");

		assertThat(browser.getSpaceURL(null), is("https://demo.backlog.jp/"));
		assertThat(browser.getProject(null), is("STWK"));
	}

}
