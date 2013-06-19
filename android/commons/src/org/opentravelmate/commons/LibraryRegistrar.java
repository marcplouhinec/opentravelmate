package org.opentravelmate.commons;

/**
 * Libraries that need to inject java objects or serve resources into web views must register it here.
 * 
 * @author Marc Plouhinec
 */
public interface LibraryRegistrar {
	
	/**
	 * Inject the given java object.
	 * 
	 * @param javaObject
	 *   Java object to inject.
	 * @param globalName
	 *   Global name of the object in the web view (for example org_opentravelmate_mapJavaObject).
	 * @param requirejsUrl
	 *   URL where the generated RequireJS-compatible JavaScript resource will be available.
	 *   Note: this JavaScript resource will look like: "define(function() { return window.org_opentravelmate_mapJavaObject; })".
	 */
	void registerJavaObject(Object javaObject, String globalName, String requirejsUrl);
	
	/**
	 * Register resources (scripts, html, css, ...) at the given URL.
	 * 
	 * @param folderClassPath
	 *   ClassPath of the folder that contains the resources to serve.
	 * @param urlPrefix
	 *   Prefix of each resource URL (for example if folderClassPath="js/map" and urlPrefix="org/opentravelmate/map",
	 *   then the resource "js/map/Map.js" will be available at "org/opentravelmate/map/Map.js").
	 */
	void registerResources(String folderClassPath, String urlPrefix);

}
