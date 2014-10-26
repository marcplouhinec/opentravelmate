package org.opentravelmate.httpserver;

import java.io.IOException;
import java.net.URLDecoder;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.commons.IOUtils;

import android.content.res.AssetManager;

/**
 * Handle requests to extension resources.
 * 
 * @author Marc Plouhinec
 */
public class ExtensionRequestHandler implements HttpRequestHandler {
	
	private final AssetManager assetManager;
	
	/**
	 * Create the request handler that provides the root document.
	 * 
	 * @param assetManager
	 */
	public ExtensionRequestHandler(AssetManager assetManager) {
		this.assetManager = assetManager;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		String target = request.getRequestLine().getUri();
		String url = URLDecoder.decode(target, "UTF-8");
		if (url != null && url.startsWith("/")) {
			url = url.substring(1);
		}
		byte[] content = IOUtils.toByteArray(assetManager.open(url));
		
		EntityTemplate entity = new EntityTemplate(new SimpleContentProducer(content));
		response.setEntity(entity);
	}
}
