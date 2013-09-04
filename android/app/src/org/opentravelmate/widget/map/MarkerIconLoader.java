package org.opentravelmate.widget.map;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opentravelmate.commons.BgThreadExecutor;
import org.opentravelmate.commons.ExceptionListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Load marker icons.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class MarkerIconLoader {
	
	private static int MAX_ICONCACHE_SIZE = 10;
	
	private final ExceptionListener exceptionListener;
	private final Map<String, Bitmap> iconCache = new LinkedHashMap<String, Bitmap>();
	
	public MarkerIconLoader(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	/**
	 * Load the given marker icon.
	 * 
	 * @param icon
	 * @param scaleRatio
	 * @param listener
	 */
	public void loadIcon(final MarkerIcon icon, final double scaleRatio, final OnIconLoadListener listener) {
		if (!(icon instanceof UrlMarkerIcon)) {
			return;
		}
		final UrlMarkerIcon urlMarkerIcon = (UrlMarkerIcon) icon;
		
		// Find the icon from the cache first
		final int iconWidth = (int)Math.round(icon.size.width * scaleRatio);
		final int iconHeight = (int)Math.round(icon.size.height * scaleRatio);
		final String cacheId = urlMarkerIcon.url + "#" + iconWidth + "," + iconHeight; 
		Bitmap cachedBitmap = getFromIconCache(cacheId);
		if (cachedBitmap != null) {
			listener.onIconLoad(cachedBitmap);
			return;
		}
		
		// Download the icon and put it in the cache
		BgThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					URL url = new URL(urlMarkerIcon.url);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream input = connection.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(input);
					Bitmap resizedBitmap = resizeBitmap(bitmap, iconWidth, iconHeight);
					addToIconCache(cacheId, resizedBitmap);
					listener.onIconLoad(resizedBitmap);
				} catch (IOException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * @param cacheId
	 * @return Bitmap from the cache.
	 */
	private Bitmap getFromIconCache(String cacheId) {
		Bitmap cachedBitmap = null;
		synchronized (iconCache) {
			cachedBitmap = iconCache.get(cacheId);
			if (cachedBitmap != null) {
				// Add the bitmap on "top" of the map
				iconCache.remove(cacheId);
				iconCache.put(cacheId, cachedBitmap);
			}
		}
		return cachedBitmap;
	}
	
	/**
	 * Add a bitmap to the cache.
	 * 
	 * @param cacheId
	 * @param bitmap
	 */
	private void addToIconCache(String cacheId, Bitmap bitmap) {
		synchronized (iconCache) {
			iconCache.put(cacheId, bitmap);
			if (iconCache.size() > MAX_ICONCACHE_SIZE) {
				String lessUsedCacheId = iconCache.keySet().iterator().next();
				iconCache.remove(lessUsedCacheId);
			}
		}
	}
	
	/**
	 * Resize a bitmap to the given width/height.
	 * 
	 * @param originalBitmap
	 * @param width
	 * @param height
	 * @return Resized bitmap
	 */
	private Bitmap resizeBitmap(Bitmap originalBitmap, int width, int height) {
		if (originalBitmap.getWidth() == width && originalBitmap.getHeight() == height) {
			return originalBitmap;
		}
		
		//Create a new bitmap and paint the resized original one
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap outputBitmap = Bitmap.createBitmap(width, height, conf);
		Canvas canvas = new Canvas(outputBitmap);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(0xFF000000);
		paint.setStrokeWidth(1);
		canvas.drawBitmap(
				originalBitmap,
				new Rect(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight()),
				new Rect(0, 0, width, height), paint);
		
		return outputBitmap;
	}
	
	/**
	 * Listener for the icon load event.
	 */
	public interface OnIconLoadListener {
		/**
		 * Function called when the icon bitmap is loaded.
		 * 
		 * @param bitmap
		 */
		public void onIconLoad(Bitmap bitmap);
	}
}
