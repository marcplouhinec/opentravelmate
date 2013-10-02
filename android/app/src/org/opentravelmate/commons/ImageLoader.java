package org.opentravelmate.commons;

import java.io.IOException;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Helper for downloading images.
 * 
 * @author Marc Plouhinec
 */
public class ImageLoader {
	
	/**
	 * Load the menu item image in background.
	 * 
	 * @param imageView
	 * @param iconUrl
	 */
	public static void loadImageForImageView(final ImageView imageView, final String iconUrl, final ExceptionListener exceptionListener) {
		BgThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					final Bitmap bitmap = IOUtils.toBitmap(iconUrl);
					UIThreadExecutor.execute(new Runnable() {
						@Override public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});
				} catch (IOException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}

}
