package hudson.plugins.backlog.api;

import hudson.plugins.backlog.api.entity.Issue;
import hudson.plugins.backlog.api.entity.Project;
import hudson.plugins.backlog.api.entity.User;
import hudson.plugins.backlog.api.util.ConvertUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * Backlog API client that wraps {@link org.apache.xmlrpc.client.XmlRpcClient}.
 * 
 * @author ikikko
 * @see http://www.backlog.jp/api/
 */
public class BacklogApiClient {

	private final XmlRpcClient client;

	public BacklogApiClient() {
		client = new XmlRpcClient();
	}

	public void login(final String space, final String userName,
			final String password) {
		final String url = "https://" + space + ".backlog.jp/XML-RPC";

		try {
			final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(url));
			config.setBasicUserName(userName);
			config.setBasicPassword(password);

			client.setConfig(config);

			getUser(userName);
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (final XmlRpcException e) {
			throw new IllegalArgumentException("Login Failed.");
		}
	}

	/**
	 * Get a {@link User} specified by {@code userId}.
	 * 
	 * @throws XmlRpcException
	 * 
	 * @see http://www.backlog.jp/api/method###.html
	 * 
	 */
	public User getUser(final String userId) throws XmlRpcException {
		final Object[] params = new Object[] { userId };

		final Object result = client.execute(Method.GET_USER.getName(), params);
		final User user = ConvertUtil.responseToUser(result);

		return user;
	}

	/**
	 * Get a {@link Project} specified by {@code key}.
	 * 
	 * @throws XmlRpcException
	 * 
	 * @see http://www.backlog.jp/api/method1_2.html
	 * 
	 */
	public Project getProject(final String key) throws XmlRpcException {
		final Object[] params = new Object[] { key };

		final Object result = client.execute(Method.GET_PROJECT.getName(),
				params);
		final Project project = ConvertUtil.responseToProject(result);

		return project;
	}

	/**
	 * Create a {@link Issue}. Return the created issue if success.
	 * 
	 * @throws XmlRpcException
	 * 
	 * @see http://www.backlog.jp/api/method4_1.html
	 * 
	 */
	public Issue createIssue(final int projectId, final Issue newIssue)
			throws XmlRpcException {
		final Map<String, Object> request = ConvertUtil
				.issueToRequest(newIssue);
		request.put("projectId", projectId);
		final Object[] params = new Object[] { request };

		final Object result = client.execute(Method.CREATE_ISSUE.getName(),
				params);
		final Issue issue = ConvertUtil.responseToIssue(result);

		return issue;
	}

}
