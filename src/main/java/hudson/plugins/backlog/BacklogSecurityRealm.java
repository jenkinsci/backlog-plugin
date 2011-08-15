package hudson.plugins.backlog;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.backlog.api.BacklogApiClient;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.dao.DataAccessException;

/**
 * {@link SecurityRealm} implementation that uses Backlog users information for
 * authentication.
 * 
 * @author ikikko
 */
public class BacklogSecurityRealm extends AbstractPasswordBasedSecurityRealm {

	private static final Log LOG = LogFactory
			.getLog(BacklogSecurityRealm.class);

	public final String url;

	@DataBoundConstructor
	public BacklogSecurityRealm(String url) {
		this.url = url;
	}

	@Override
	protected UserDetails authenticate(String username, String password)
			throws AuthenticationException {

		try {
			new BacklogApiClient().login(url, username, password);
			return new User(username, "", true, true, true, true,
					new GrantedAuthority[] { AUTHENTICATED_AUTHORITY });

		} catch (Exception e) {
			throw new BadCredentialsException("Failed to login as " + username,
					e);
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
				new BacklogApiClient().getEntryPointURL(url);
			} catch (IllegalArgumentException e) {
				return FormValidation.error(Messages
						.BacklogSecurityRealm_Url_Error());
			}

			return FormValidation.ok();
		}
	}

}
