package org.opentravelmate.launcher.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.util.Log;

/**
 * Handle requests to JavaScript APIs.
 * 
 * @author Marc Plouhinec
 */
public class JavascriptRequestHandler implements HttpRequestHandler {
	
	private Map<String, String> injectedJavaObjectNameByURL = new HashMap<String, String>();
	private Map<String, String> classPathFolderByUrlPrefix = new HashMap<String, String>();
	
	/**
	 * Register an injected Java Object. A Request to the given URL will trigger a
	 * Require.JS-compatible script that returns 'window.objectName'.
	 * 
	 * @param url
	 *   URL of the injected java object.
	 * @param objectName
	 *   Name of the JavaObject.
	 */
	public void registerInjectedJavaObject(String url, String objectName) {
		injectedJavaObjectNameByURL.put(url, objectName);
	}
	
	/**
	 * Register resources (scripts, html, css, ...) at the given URL.
	 * 
	 * @param folderClassPath
	 *   ClassPath of the folder that contains the resources to serve.
	 * @param urlPrefix
	 *   Prefix of each resource URL (for example if folderClassPath="js/map" and urlPrefix="org/opentravelmate/map",
	 *   then the resource "js/map/Map.js" will be available at "org/opentravelmate/map/Map.js").
	 */
	public void registerResources(String folderClassPath, String urlPrefix) {
		classPathFolderByUrlPrefix.put(urlPrefix, folderClassPath);
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		String target = request.getRequestLine().getUri();
		String url = URLDecoder.decode(target, "UTF-8");
		
		// Load the required resources
		byte[] content;
		if (injectedJavaObjectNameByURL.containsKey(url)) {
			StringBuilder stringBuilder = new StringBuilder()
				.append("define([], function() {\n")
				.append("  return window.").append(injectedJavaObjectNameByURL.get(url)).append(";\n")
				.append("});");
			content = stringBuilder.toString().getBytes("UTF-8");
		} else {
			InputStream inputStream = findMatchingResource(url);
			if (inputStream == null) {
				throw new IOException("Unknown resource: " + url);
			}
			content = IOUtils.toByteArray(inputStream);
			IOUtils.closeQuietly(inputStream);
		}
		
		// Send the resource
		Log.i("TEST", new String(content));
		EntityTemplate entity = new EntityTemplate(new SimpleContentProducer(content));
		response.setEntity(entity);
	}
	
	/**
	 * Find the resource that matches the given url.
	 * 
	 * @param url
	 * @return resource input stream or null if not found
	 */
	private InputStream findMatchingResource(String url) {
		for (Map.Entry<String, String> entry : classPathFolderByUrlPrefix.entrySet()) {
			String urlPrefix = entry.getKey();
			if (url.startsWith(urlPrefix)) {
				String urlSuffix = url.substring(urlPrefix.length());
				String folderClassPath = entry.getValue();
				return this.getClass().getResourceAsStream(folderClassPath + urlSuffix);
			}
		}
		return null;
	}

}
