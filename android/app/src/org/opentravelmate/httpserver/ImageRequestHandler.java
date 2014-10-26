package org.opentravelmate.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;

/**
 * Intercept the loading of images in order to read/write them from/into a disk cache and to apply some filters.
 * The URL must be in the format: /image/source/<source URL>?filter=grayscale
 * The query part after the ? (included) is optional.
 * 
 * @author Marc Plouhinec
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
		try {
			this.diskLruCache.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable to close the disk LRU cache.", e);
		}
	}

	@Override
	public void handle(HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
		String urlAsString = request.getRequestLine().getUri();
		
		// Extract the image source and parse the filter parameter
		if (!urlAsString.startsWith("/image/source/")) {
			throw new HttpException("Unknown request");
		}
		String query = urlAsString.substring("/image/source/".length());
		boolean applyGrayscaleFilter = false;
		int optionsIndex = query.lastIndexOf('?');
		if (optionsIndex != -1) {
			String urlOptionalParameters = query.substring(optionsIndex + 1);
			query = query.substring(0, optionsIndex);
			String filter = this.parseFilterParameter(urlOptionalParameters);
			applyGrayscaleFilter = "grayscale".equals(filter);
		}
		String imageSource = URLDecoder.decode(urlAsString.substring("/image/source/".length()), "UTF-8");
		
		// Load the image data and apply the grayscale filter if necessary
		byte[] imageData = loadImageData(imageSource);
		if (applyGrayscaleFilter) {
			imageData = this.applyGrayscaleFilter(imageData);
		}
		
		// Send the image
		response.setEntity(new EntityTemplate(new SimpleContentProducer(imageData)));
	}
	
	/**
	 * Load the given image from Internet or a cache and cache it if necessary.
	 * 
	 * @param imageSource
	 * @return image data
	 * @throws IOException
	 */
	private byte[] loadImageData(String imageSource) throws IOException {
		// Load the image from the cache if possible
		String cacheKey = this.generateCacheKey(imageSource);
		Snapshot snapshot = this.diskLruCache.get(cacheKey);
		if (snapshot != null) {
			byte[] imageData = IOUtils.toByteArray(snapshot.getInputStream(0));
			snapshot.close();
			return imageData;
		}
		
		// Download the image
		URL imageUrl = new URL(imageSource);
		byte[] imageData = IOUtils.toByteArray(imageUrl.openStream());
		
		// Put the image in the cache
		Editor editor = this.diskLruCache.edit(cacheKey);
		OutputStream outputStream = editor.newOutputStream(0);
		outputStream.write(imageData);
		outputStream.close();
		editor.commit();
		
		return imageData;
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
	 * Convert the color image into a grayscale one.
	 * Thanks to http://stackoverflow.com/a/3391061
	 * 
	 * @param imageData
	 * @return converted image
	 */
	private byte[] applyGrayscaleFilter(byte[] imageData) {
		// Apply the grayscale filter
		Bitmap bmpOriginal = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		Bitmap bmpGrayscale = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
		paint.setColorFilter(filter);
		canvas.drawBitmap(bmpOriginal, 0, 0, paint);
		
		// Convert the new bitmap into a byte array
		CompressFormat compressFormat = findCompressFormat(imageData);
		if (compressFormat == null) {
			ByteBuffer byteBuffer = ByteBuffer.allocate(bmpGrayscale.getRowBytes() * bmpGrayscale.getHeight());
			bmpGrayscale.copyPixelsToBuffer(byteBuffer);
			return byteBuffer.array();
		} else {
			ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
			bmpGrayscale.compress(compressFormat, 70, byteArrayBitmapStream);
			return byteArrayBitmapStream.toByteArray();
		}
	}
	
	/**
	 * Find the compress format of the given image data.
	 * 
	 * @param imageData
	 * @return compress format
	 */
	private Bitmap.CompressFormat findCompressFormat(byte[] imageData) {
		if (imageData == null) {
			return null;
		}

		if (imageData.length > 4 && imageData[0] == 'R' && imageData[1] == 'I' && imageData[2] == 'F' && imageData[3] == 'F') {
			return Bitmap.CompressFormat.JPEG;
		}
		if (imageData.length > 4 && imageData[1] == 'P' && imageData[2] == 'N' && imageData[3] == 'G') {
			return Bitmap.CompressFormat.PNG;
		}
		if (imageData.length > 2 && imageData[0] == 0xFF && imageData[1] == 0xD8) {
			return Bitmap.CompressFormat.JPEG;
		}
		return null;
	}
	
	/**
	 * Parse the URL optional parameters and find the "filter" parameter.
	 * 
	 * @param urlOptionalParameters
	 * @return filter parameter value or null if not found
	 */
	private String parseFilterParameter(String urlOptionalParameters) {
		String[] optionalParameters = urlOptionalParameters.split("[?&]{1}");
		for (String parameter : optionalParameters) {
			String[] keyValue = parameter.split("=");
			if ("filter".equals(keyValue[0])) {
				return keyValue[1];
			}
		}
		return null;
	}
}
