package org.opentravelmate.httpserver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.I18nException;

import android.util.Log;


/**
 * Serve the documents for the web views.
 * 
 * Thanks to: http://www.docjar.com/html/api/org/apache/http/examples/ElementalHttpServer.java.html
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class HttpServer {
	
	private final Map<String, HttpRequestHandler> requestHandlerByPattern;
	private final ExceptionListener exceptionListener;
	private final ExecutorService executorService = new ThreadPoolExecutor(4, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private ServerSocket serverSocket;
	
	/**
	 * Build a HTTP server.
	 * 
	 * @param requestHandlerByPattern
	 *   Map<pattern, HTTP request handler>
	 * @param exceptionListener
	 *   Listener called when a server exception is thrown (can be used to display an error message to the user).
	 */
	public HttpServer(Map<String, HttpRequestHandler> requestHandlerByPattern, ExceptionListener exceptionListener) {
		this.requestHandlerByPattern = requestHandlerByPattern;
		this.exceptionListener = exceptionListener;
	}
	
	/**
	 * Create a server socket on an available port and process the requests.
	 */
	public void start() throws IOException {
		// Prepare the HTTP server
		this.serverSocket = new ServerSocket(0);
		
		HttpParams httpParams = new BasicHttpParams()
			.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
			.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
			.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
			.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
			.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
		
		BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
		httpProcessor.addInterceptor(new ResponseDate());
		httpProcessor.addInterceptor(new ResponseServer());
		httpProcessor.addInterceptor(new ResponseContent());
		httpProcessor.addInterceptor(new ResponseConnControl());
		
		HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
		for (Map.Entry<String, HttpRequestHandler> entry : requestHandlerByPattern.entrySet()) {
			registry.register(entry.getKey(), entry.getValue());
		}
		
		HttpService httpService = new HttpService(httpProcessor, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
		httpService.setParams(httpParams);
		httpService.setHandlerResolver(registry);
		
		// Handle incoming connections
		executorService.execute(new RequestListener(this.serverSocket, httpParams, httpService, executorService, exceptionListener));
 	}
	
	/**
	 * Stop the server.
	 */
	public void stop() {
		executorService.shutdownNow();
	}
	
	/**
	 * @return listening port of the server.
	 */
	public int getPort() {
		return this.serverSocket.getLocalPort();
	}
	
	/**
	 * Listen to an incoming request and process it in a new thread.
	 */
	private static class RequestListener implements Runnable {
		
		private final static String LOG_TAG = "RequestListener";
		private final ServerSocket serverSocket;
		private final HttpParams httpParams;
		private final HttpService httpService;
		private final ExecutorService executorService;
		private final ExceptionListener exceptionListener;
		
		/**
		 * Create a request listener.
		 * 
		 * @param serverSocket
		 * @param httpParams
		 * @param httpService
		 * @param executorService
		 * @param exceptionListener
		 */
		public RequestListener(
				ServerSocket serverSocket,
				HttpParams httpParams,
				HttpService httpService,
				ExecutorService executorService,
				ExceptionListener exceptionListener) {
			this.serverSocket = serverSocket;
			this.httpParams = httpParams;
			this.httpService = httpService;
			this.executorService = executorService;
			this.exceptionListener = exceptionListener;
		}
		
		@Override public void run() {
			int localPort = this.serverSocket.getLocalPort();
			Log.i(LOG_TAG, "Listening on port " + localPort);
			
			while (!Thread.interrupted()) {
				try {
					Socket socket = this.serverSocket.accept();
					DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
					connection.bind(socket, this.httpParams);
					executorService.execute(new RequestHandler(httpService, connection, exceptionListener));
					
				} catch (InterruptedIOException e) {
					exceptionListener.onException(true, new I18nException(
							R.string.httpserver_error_unable_to_initialize_connection, e, localPort));
					break;
				} catch (IOException e) {
					exceptionListener.onException(true, new I18nException(
							R.string.httpserver_error_unable_to_initialize_connection, e, localPort));
					break;
				}
			}
		}
	};
	
	/**
	 * Handle one request.
	 */
	private static class RequestHandler implements Runnable {
		
		private final HttpService httpService;
		private final HttpServerConnection connection;
		private final ExceptionListener exceptionListener;

		/**
		 * Create a request handler.
		 * 
		 * @param httpService
		 * @param connection
		 * @param exceptionListener
		 */
		public RequestHandler(HttpService httpService, HttpServerConnection connection, ExceptionListener exceptionListener) {
			this.httpService = httpService;
			this.connection = connection;
			this.exceptionListener = exceptionListener;
		}

		@Override
		public void run() {
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.connection.isOpen()) {
					this.httpService.handleRequest(this.connection, context);
				}
			} catch (ConnectionClosedException e) {
				exceptionListener.onException(false, new I18nException(R.string.httpserver_error_connection_closed, e));
			} catch (IOException e) {
				exceptionListener.onException(false, new I18nException(R.string.httpserver_error_io_error, e));
			} catch (HttpException e) {
				exceptionListener.onException(false, new I18nException(R.string.httpserver_error_http_error, e));
			} finally {
				try {
					this.connection.shutdown();
				} catch (IOException ignore) {
					// Do nothing
				}
			}
		}
		
	}
}
