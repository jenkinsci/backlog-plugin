package hudson.plugins.backlog.webdav;

import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;
import hudson.FilePath;
import hudson.plugins.backlog.Messages;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;


public class WebdavClient {

	private final SardineImpl sardine;

	private final String url;

	private String removePrefix = "";

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

	public void put(FilePath filePath, String remotePath) throws IOException {
		HttpEntity entity = new FilePathEntity(filePath,
				HTTP.DEFAULT_CONTENT_TYPE);

		sardine.put(url + remotePath + filePath.getName(), entity,
				HTTP.DEFAULT_CONTENT_TYPE, true);
	}

	public void putWithParent(FilePath filePath, String remotePath,
			FilePath basePath) throws IOException, InterruptedException {
		String directory = normalizeDirectory(remotePath);

		createDirectoriesRecursive("", directory);
		createDirectoriesFromBase(filePath.getParent(), directory, basePath);
		put(filePath,
				directory + getPathFromBase(filePath.getParent(), basePath));
	}

	public boolean delete(String remotePath) throws IOException {
		String deleteUrl = url + normalizeDirectory(remotePath);

		if (sardine.exists(deleteUrl)) {
			sardine.delete(deleteUrl);
			return true;
		} else {
			return false;
		}
	}

	// -------------------------------------- helper method (package private)

	void createDirectory(String remotePath) throws IOException {
		String createUrl = normalizeDirectory(url + remotePath);

		if (!sardine.exists(createUrl)) {
			sardine.createDirectory(createUrl);
		}
	}

	void createDirectoriesFromBase(FilePath filePath, String remotePath,
			FilePath basePath) throws IOException, InterruptedException {
		createDirectoriesRecursive(remotePath,
				getPathFromBase(filePath, basePath));
	}

	void createDirectoriesRecursive(String base, String path)
			throws IOException {

		String parent = normalizeDirectory(base);
		createDirectory(parent);

		for (String splitPath : path.split("/")) {
			parent = normalizeDirectory(parent + splitPath);
			createDirectory(parent);
		}
	}

	String getPathFromBase(FilePath filePath, FilePath basePath)
			throws IOException, InterruptedException {
		String pathString = filePath.toURI().normalize().getPath();
		String baseString = basePath.toURI().normalize().getPath();
		String pathFromBase = pathString.substring(baseString.length());

		if (!pathFromBase.startsWith(normalizeDirectory(removePrefix))) {
			throw new IllegalArgumentException(
					Messages.BacklogWebdavPublisher_ErrorNotStartedPrefix(
							pathFromBase, removePrefix));
		}
		return pathFromBase
				.substring(normalizeDirectory(removePrefix).length());
	}

	String normalizeDirectory(String directory) {
		if (directory.isEmpty()) {
			return directory;
		}

		String result;
		if (directory.startsWith("/")) {
			result = directory.substring(1);
		} else {
			result = directory;
		}

		if (!directory.endsWith("/")) {
			result = result + "/";
		}

		return result;
	}

	// -------------------------------------- getter/setter

	public String getRemovePrefix() {
		return removePrefix;
	}

	public void setRemovePrefix(String removePrefix) {
		this.removePrefix = removePrefix;
	}

}
