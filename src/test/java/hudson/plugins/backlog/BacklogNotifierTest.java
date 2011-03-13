package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.util.FormValidation;

import org.junit.Before;
import org.junit.Test;

public class BacklogNotifierTest {

	private BacklogNotifier.DescriptorImpl desc;

	@Before
	public void setUp() {
		desc = new BacklogNotifier.DescriptorImpl();
	}

	@Test
	public void doCheckSpace_ok() throws Exception {
		FormValidation actual;

		actual = desc.doCheckSpace("");
		assertValidationOk(actual);

		actual = desc.doCheckSpace("space-1");
		assertValidationOk(actual);
	}

	@Test
	public void doCheckSpace_error() throws Exception {
		FormValidation actual;

		actual = desc.doCheckSpace("12");
		assertValidationError(actual);

		actual = desc.doCheckSpace("12345678901");
		assertValidationError(actual);

		actual = desc.doCheckSpace("http://space.backlog.jp/");
		assertValidationError(actual);
	}

	@Test
	public void doCheckProjectKey_ok() throws Exception {
		FormValidation actual;

		actual = desc.doCheckProjectKey("");
		assertValidationOk(actual);

		actual = desc.doCheckProjectKey("PROJECT1");
		assertValidationOk(actual);
	}

	@Test
	public void doCheckProjectKey_error() throws Exception {
		FormValidation actual;

		actual = desc.doCheckProjectKey("project1");
		assertValidationError(actual);

		actual = desc.doCheckSpace("PROJECT-1");
		assertValidationError(actual);

		actual = desc.doCheckSpace("https://space.backlog.jp/projects/PROJECT");
		assertValidationError(actual);
	}

	@Test
	public void doCheckUserId_ok() throws Exception {
		FormValidation actual;

		actual = desc.doCheckUserId("");
		assertValidationOk(actual);

		actual = desc.doCheckUserId("user-ID_1");
		assertValidationOk(actual);
	}

	@Test
	public void doCheckUserIdKey_error() throws Exception {
		FormValidation actual;

		actual = desc.doCheckUserId("user.id");
		assertValidationError(actual);

		actual = desc.doCheckUserId("user@id");
		assertValidationError(actual);
	}

	@Test
	public void doCreateTestIssue_skip() throws Exception {
		FormValidation actual;

		actual = desc.doCreateTestIssue("space", "projectKey", "userId", "");
		assertValidationWarning(actual);

		actual = desc.doCreateTestIssue("space", "projectKey", "", "password");
		assertValidationWarning(actual);

		actual = desc.doCreateTestIssue("space", "", "userId", "password");
		assertValidationWarning(actual);

		actual = desc.doCreateTestIssue("", "projectKey", "userId", "password");
		assertValidationWarning(actual);
	}

	@Test
	public void doCreateTestIssue_error() throws Exception {
		FormValidation actual;

		actual = desc.doCreateTestIssue("fail", "fail", "fail", "fail");
		assertValidationError(actual);
	}

	private static void assertValidationOk(FormValidation actual) {
		assertThat(actual.kind, is(FormValidation.Kind.OK));
	}

	private static void assertValidationWarning(FormValidation actual) {
		assertThat(actual.kind, is(FormValidation.Kind.WARNING));
	}

	private static void assertValidationError(FormValidation actual) {
		assertThat(actual.kind, is(FormValidation.Kind.ERROR));
	}

}
