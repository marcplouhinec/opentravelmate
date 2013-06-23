package org.opentravelmate.launcher.httpserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Minimalist version of Apache Commons IOUtils.
 * 
 * @author Marc Plouhinec
 */
public class IOUtils {

	/**
	 * Read data from an input stream.
	 * 
	 * @param inputStream
	 * @return byte array
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		byte[] buffer = new byte[1024];
		int len;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		while ((len = bufferedInputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		
		return outputStream.toByteArray();
	}
	
	/**
	 * Close an input stream without throwing an exception.
	 * 
	 * @param inputStream
	 */
	public static void closeQuietly(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
	
}
