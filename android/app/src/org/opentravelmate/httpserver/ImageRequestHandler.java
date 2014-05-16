package org.opentravelmate.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.commons.IOUtils;

import android.net.Uri;
import android.util.Log;

/**
 * Intercept the loading of images in order to read/write them from/into a disk cache and to apply some filters.
 * The URL must be in the format: /image/source/<source URL>
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class ImageRequestHandler implements HttpRequestHandler {

	@Override
	public void handle(HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
		String urlAsString = request.getRequestLine().getUri();
		
		// Extract the image source
		if (!urlAsString.startsWith("/image/source/")) {
			throw new HttpException("Unknown request");
		}
		String imageSource = URLDecoder.decode(urlAsString.substring("/image/source/".length()), "UTF-8");
		Uri imageUri = Uri.parse(imageSource);
		Log.i("TEST", "imageUri = " + imageUri);
		
		// Download the image
		InputStream inputStream = null;
		byte[] imageData;
		try {
			URL imageUrl = new URL(imageSource);
			inputStream = imageUrl.openStream();
			imageData = IOUtils.toByteArray(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		
		// Send the image
		EntityTemplate entity = new EntityTemplate(new SimpleContentProducer(imageData));
		response.setEntity(entity);
	}

}
