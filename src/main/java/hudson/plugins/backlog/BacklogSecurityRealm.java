package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.util.FormValidation;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@link SecurityRealm} implementation that uses Backlog users information for
 * authentication.
 * 
 * @author ikikko
 */
public class BacklogSecurityRealm extends AbstractPasswordBasedSecurityRealm {

	/** Endpoint which a user who has an account can access and independents of a project */
	private static final String LOGIN_ENDPOINT = "ical/myissues.ics";

	private static final Log LOG = LogFactory
			.getLog(BacklogSecurityRealm.class);

	public final String url;

	@DataBoundConstructor
	public BacklogSecurityRealm(String url) {
		// normalize
		if (StringUtils.isNotEmpty(url)) {
			if (url.endsWith("/")) {
				this.url = url;
			} else {
				this.url = url + '/';
			}
		} else {
			this.url = null;
		}
	}

	@Override
	protected UserDetails authenticate(String username, String password)
			throws AuthenticationException {
		try {
			if (canLogin(username, password)) {
                return new User(username, "", true, true, true, true,
                        new GrantedAuthority[] { AUTHENTICATED_AUTHORITY });
            } else {
				throw new BadCredentialsException("Failed to login as " + username);
			}
		} catch (IOException e) {
			throw new BadCredentialsException("Failed to login as " + username, e);
		}

	}

	private boolean canLogin(String username, String password) throws IOException {
		HttpGet httpGet = new HttpGet(url + LOGIN_ENDPOINT);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(httpGet.getURI().getHost(), httpGet.getURI().getPort()),
				new UsernamePasswordCredentials(username, password));

		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			EntityUtils.consume(response.getEntity()); // for closing entity ( not needed? )

			return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		LOG.info("Backlog Security Realm always returns the specified user : '"
				+ username + "'");
		return new User(username, "", true, true, true, true,
				new GrantedAuthority[] { AUTHENTICATED_AUTHORITY });
	}

	@Override
	public GroupDetails loadGroupByGroupname(String groupname)
			throws UsernameNotFoundException, DataAccessException {
		LOG.warn("Backlog Security Realm doesn't support groups : '"
				+ groupname + "'");
		throw new UsernameNotFoundException(groupname);
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
		public String getDisplayName() {
			return "Backlog";
		}

		public FormValidation doCheckUrl(@QueryParameter String url) {
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				return FormValidation.error(Messages
						.BacklogSecurityRealm_Url_Error());
			}

			return FormValidation.ok();
		}
	}

}
