package hudson.plugins.backlog.webdav;

import hudson.FilePath;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;

import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.impl.SardineImpl;

public class WebdavClient {

	private final SardineImpl sardine;

	private final String url;

	private String removePrefixDirectory = "";

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

	// if i can
	// TODO add option : multiple includes files
	// TODO add option : flatten
	// TODO implement : mkdir root directory
	// (if can't, remote directory is required)

	public void putWithParent(FilePath filePath, String remotePath,
			FilePath basePath) throws IOException, InterruptedException {
		String directory = normalizeDirectory(remotePath);

		createDirectory(directory);
		createDirectoriesFromBase(filePath.getParent(), directory, basePath);
		put(filePath,
				directory + getPathFromBase(filePath.getParent(), basePath));
	}

	public boolean delete(String remotePath) throws IOException {
		String deleteUrl = normalizeDirectory(url + remotePath);

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
		String parent = normalizeDirectory(remotePath);
		String pathFromBaseDir = getPathFromBase(filePath, basePath);

		for (String path : pathFromBaseDir.split("/")) {
			parent = normalizeDirectory(parent + path);
			createDirectory(parent);
		}
	}

	String getPathFromBase(FilePath filePath, FilePath basePath)
			throws IOException, InterruptedException {
		String pathString = filePath.toURI().normalize().getPath();
		String baseString = basePath.toURI().normalize().getPath();
		String pathFromBase = pathString.substring(baseString.length());

		if (!pathFromBase
				.startsWith(normalizeRemovePrefixDirectory(removePrefixDirectory))) {
			// TODO i18n
			throw new IllegalArgumentException(
					"If you use remove prefix, then ALL source file paths MUST start with the prefix.");
		}
		return pathFromBase.substring(normalizeRemovePrefixDirectory(
				removePrefixDirectory).length());
	}

	String normalizeRemovePrefixDirectory(String directory) {
		if (directory.isEmpty()) {
			return normalizeDirectory(directory);
		} else if (directory.startsWith("/")) {
			return normalizeDirectory(directory.substring(1));
		} else {
			return normalizeDirectory(directory);
		}
	}

	String normalizeDirectory(String directory) {
		if (directory.isEmpty()) {
			return directory;
		} else if (directory.endsWith("/")) {
			return directory;
		} else {
			return directory + "/";
		}
	}

	// -------------------------------------- getter/setter

	public String getRemovePrefixDirectory() {
		return removePrefixDirectory;
	}

	public void setRemovePrefixDirectory(String removePrefixDirectory) {
		this.removePrefixDirectory = removePrefixDirectory;
	}

}
