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
