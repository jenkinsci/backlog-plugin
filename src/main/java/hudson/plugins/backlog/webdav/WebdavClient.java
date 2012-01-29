package hudson.plugins.backlog.webdav;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HTTP;

import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.impl.SardineImpl;

public class WebdavClient {

	private final SardineImpl sardine;

	private final String url;

	public WebdavClient(String url, String username, String password) {
		this.url = url;

		sardine = (SardineImpl) SardineFactory.begin(username, password);

		// confirm parameters
		try {
			sardine.list(url);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed login to Backlog", e);
		}
	}

	public void put(File file, String path) throws IOException {
		HttpEntity entity = new FileEntity(file, HTTP.DEFAULT_CONTENT_TYPE);

		sardine.put(url + path + file.getName(), entity,
				HTTP.DEFAULT_CONTENT_TYPE, true);
	}

}
