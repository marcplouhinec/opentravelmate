package org.opentravelmate.commons;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utilities for IO and HTTP.
 * 
 * @author Marc Plouhinec
 */
public class IOUtils {

	/**
	 * Read data from an input stream and close it quietly.
	 * 
	 * @param inputStream
	 * @return byte array
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			byte[] buffer = new byte[1024];
			int len;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			while ((len = bufferedInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			
			return outputStream.toByteArray();
		} finally {
			closeQuietly(inputStream);
		}
	}
	
	/**
	 * Close an closeable without throwing an exception.
	 * 
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
	
	/**
	 * Load a bitmap from an URL.
	 * Thanks to http://stackoverflow.com/questions/844972/is-it-possible-to-put-an-image-from-a-url-in-a-imagebutton-in-android
	 * 
	 * @param bitmapUrl
	 * @return loaded bitmap
	 * @throws IOException
	 */
	public static Bitmap toBitmap(String bitmapUrl) throws IOException {
		HttpGet httpRequest = new HttpGet(bitmapUrl);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
		
		HttpEntity entity = response.getEntity();
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		byte[] bitmapContent = toByteArray(bufHttpEntity.getContent());
		return BitmapFactory.decodeByteArray(bitmapContent, 0, bitmapContent.length);
	}
}
