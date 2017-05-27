package hudson.plugins.backlog.webdav;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

/**
 * HttpEntity that uses {@link FilePath} instead of {@link File}.
 * 
 * @see org.apache.http.entity.FileEntity
 * @see org.apache.http.entity.InputStreamEntity
 * 
 * @author ikikko
 */
public class FilePathEntity extends AbstractHttpEntity {

	protected final FilePath filePath;

	public FilePathEntity(FilePath filePath, String contentType) {
		super();
		if (filePath == null) {
			throw new IllegalArgumentException("FilePath may not be null");
		}
		this.filePath = filePath;
		setContentType(contentType);
	}

	public boolean isRepeatable() {
		return true;
	}

	public long getContentLength() {
		try {
			return filePath.length();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public InputStream getContent() throws IOException, IllegalStateException {
		try {
			return filePath.read();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeTo(OutputStream outstream) throws IOException {
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		InputStream instream;
		try {
			instream = filePath.read();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				outstream.write(tmp, 0, l);
			}
			outstream.flush();
		} finally {
			instream.close();
		}
	}

	public boolean isStreaming() {
		return false;
	}

}
