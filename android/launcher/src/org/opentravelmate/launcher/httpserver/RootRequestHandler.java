package org.opentravelmate.launcher.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.res.AssetManager;

/**
 * Handle request to the root document.
 * 
 * @author Marc Plouhinec
 */
public class RootRequestHandler implements HttpRequestHandler {
	
	private final AssetManager assetManager;
	
	/**
	 * Create the request handler that provides the root document.
	 * 
	 * @param assetManager
	 */
	public RootRequestHandler(AssetManager assetManager) {
		this.assetManager = assetManager;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		String target = request.getRequestLine().getUri();
		String url = URLDecoder.decode(target, "UTF-8");
		
		InputStream inputStream = null;
		byte[] content;
		try {
			inputStream = assetManager.open("rootdocument" + url);
			content = IOUtils.toByteArray(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		
		EntityTemplate entity = new EntityTemplate(new SimpleContentProducer(content));
		response.setEntity(entity);
	}
}
