package hudson.plugins.backlog.repositorybrowser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class RepositoryBrowserHelperTest {

	@Test
	public void testGetSpaceURLAndProjectWhenOptionUrlset() throws Exception {
		RepositoryBrowserHelper helper = new RepositoryBrowserHelper(
				"https://demo.backlog.jp/projects/STWK");

		assertThat(helper.getSpaceURL(null), is("https://demo.backlog.jp/"));
		assertThat(helper.getProject(null), is("STWK"));
	}

}
