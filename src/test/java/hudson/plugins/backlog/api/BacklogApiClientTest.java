package hudson.plugins.backlog.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import hudson.plugins.backlog.api.entity.Issue;
import hudson.plugins.backlog.api.entity.Project;
import hudson.plugins.backlog.api.entity.User;
import hudson.plugins.backlog.base.BaseTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BacklogApiClientTest extends BaseTest {

	private BacklogApiClient client;

	@Before
	public void setUp() throws Exception {
		client = new BacklogApiClient();
		client.login(BACKLOG_SPACE_URL, BACKLOG_USERNAME, BACKLOG_PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void loginFail() throws Exception {
		client.login("https://fail.backlog.jp/", "fail", "fail");
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedUrl() throws Exception {
		client.getEntryPointURL("malformed_url.backlog.jp");
	}

	@Test
	public void getProject() throws Exception {
		final Project project = client.getProject(BACKLOG_PROJECT_KEY);

		assertThat(project, is(notNullValue()));
	}

	@Test
	public void getUser() throws Exception {
		final User user = client.getUser(BACKLOG_USERNAME);

		assertThat(user, is(notNullValue()));
	}

	@Ignore("更新系APIは、普段のユニットテストでは実行しない")
	@Test
	public void createIssue() throws Exception {
		final Issue newIssue = new Issue();
		newIssue.setSummary("Backlog API テスト登録");

		final Issue issue = client.createIssue(BACKLOG_PROJECT_ID, newIssue);

		assertThat(issue, is(notNullValue()));
	}

}
