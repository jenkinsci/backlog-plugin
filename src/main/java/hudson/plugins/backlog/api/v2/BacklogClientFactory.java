package hudson.plugins.backlog.api.v2;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogPackageConfigure;
import hudson.plugins.backlog.BacklogProjectProperty;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;

/**
 * @author ikikko
 */
public class BacklogClientFactory {

    public static BacklogClient getBacklogClient(BacklogProjectProperty bpp) throws MalformedURLException{
        // check project property parameter
        if (bpp == null) {
            throw new IllegalArgumentException("'Backlog property' is not set. Can't comment a pull request.");
        }
        if (StringUtils.isEmpty(bpp.getSpaceURL())) {
            throw new IllegalArgumentException("'Backlog URL' is not set. Can't comment a pull request.");
        }
        if (StringUtils.isEmpty(bpp.getProject())) {
            throw new IllegalArgumentException("'project' is not included in Backlog URL. Can't comment a pull request.");
        }
        if (StringUtils.isEmpty(bpp.getApiKey().getPlainText())) {
            throw new IllegalArgumentException("'apiKey' is not set. Can't comment a pull request.");
        }

        if (Jenkins.getInstance().getPlugin("git") == null) {
            throw new IllegalArgumentException("This project doesn't use Git as SCM. Can't comment a pull request.");
        }

        BacklogConfigure configure = new BacklogPackageConfigure(bpp.getSpaceURL()).apiKey(bpp.getApiKey().getPlainText());
        return new com.nulabinc.backlog4j.BacklogClientFactory(configure).newClient();
    }

}
