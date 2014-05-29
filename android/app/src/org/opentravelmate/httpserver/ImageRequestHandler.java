package org.opentravelmate.httpserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.commons.IOUtils;

import android.content.Context;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;

/**
 * Intercept the loading of images in order to read/write them from/into a disk cache and to apply some filters.
 * The URL must be in the format: /image/source/<source URL>
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class ImageRequestHandler implements HttpRequestHandler {
	
	private static final int MAX_CACHE_SIZE = 20 * 1024 * 1024 * 8;
	private static final String LOG_TAG = "ImageRequestHandler";
	
	private final DiskLruCache diskLruCache;
	
	public ImageRequestHandler(Context context) {
		try {
			File cacheDir = new File(context.getCacheDir().getPath() + File.separator + "ImageRequestHandlerCache");
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			this.diskLruCache = DiskLruCache.open(cacheDir, 1, 1, MAX_CACHE_SIZE);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to initialize the image cache.");
		}
	}
	
	/**
	 * Quietly close the {@link ImageRequestHandler}.
	 */
	public void close() {
		Log.i(LOG_TAG, "Close the ImageRequestHandler.");
		try {
			this.diskLruCache.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable to close the disk LRU cache.", e);
		}
	}

	@Override
	public void handle(HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
		String urlAsString = request.getRequestLine().getUri();
		
		// Extract the image source
		if (!urlAsString.startsWith("/image/source/")) {
			throw new HttpException("Unknown request");
		}
		String imageSource = URLDecoder.decode(urlAsString.substring("/image/source/".length()), "UTF-8");
		
		// Load the image from the cache if possible
		String cacheKey = this.generateCacheKey(imageSource);
		Snapshot snapshot = this.diskLruCache.get(cacheKey);
		if (snapshot != null) {
			Log.i(LOG_TAG, "Load the image " + imageSource + " from the cache.");
			byte[] imageData = this.loadImage(snapshot.getInputStream(0));
			snapshot.close();
			response.setEntity(new EntityTemplate(new SimpleContentProducer(imageData)));
			return;
		}
		
		// Download the image
		Log.i(LOG_TAG, "Load the image " + imageSource + " from internet.");
		URL imageUrl = new URL(imageSource);
		byte[] imageData = this.loadImage(imageUrl.openStream());
		
		// Put the image in the cache
		Editor editor = this.diskLruCache.edit(cacheKey);
		OutputStream outputStream = editor.newOutputStream(0);
		outputStream.write(imageData);
		outputStream.close();
		editor.commit();
		
		// Send the image
		response.setEntity(new EntityTemplate(new SimpleContentProducer(imageData)));
	}

	/**
	 * Generate a key that respects the {@link DiskLruCache} requirement: [a-z0-9_-]{1,64}.
	 * 
	 * @param imageSource
	 * @return Cache key
	 */
	private String generateCacheKey(String imageSource) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(imageSource.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			return number.toString(16);
		} catch (NoSuchAlgorithmException e) {
			return String.valueOf(imageSource.hashCode());
		}
	}
	
	/**
	 * Load data from an {@link InputStream} and close it properly.
	 * 
	 * @param inputStream
	 * @return data from the stream
	 * @throws IOException
	 */
	private byte[] loadImage(InputStream inputStream) throws IOException {
		byte[] imageData = null;
		try {
			imageData = IOUtils.toByteArray(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return imageData;
	}
}
