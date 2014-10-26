package org.opentravelmate.commons;

import java.util.concurrent.Callable;

import android.os.Handler;

/**
 * Run code in the UI thread.
 * 
 * @author Marc Plouhinec
 */
public class UIThreadExecutor {
	
	private static Handler handler = null;
	private static Thread uiThread = null;
	
	/**
	 * Initialize this class.
	 * 
	 * Note: this method must be called in the UI thread.
	 */
	public static void init() {
		handler = new Handler();
		uiThread = Thread.currentThread();
	}
	
	/**
	 * Run the given runnable in the UI thread.
	 * 
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		handler.post(runnable);
	}
	
	/**
	 * Execute the given Callable in the UI thread synchronously.
	 * The function returns when the callable has been executed.
	 * 
	 * @param callable
	 * @param sleepingTime Time to wait between two callable ending check.
	 * @return callable result
	 * @throws Exception
	 */
	public static <T> T executeSync(final Callable<T> callable, long sleepingTime) throws Exception {
		if (Thread.currentThread().equals(uiThread)) {
			return callable.call();
		}
		
		CallableWrapper<T> callableWrapper = new CallableWrapper<T>(callable);
		handler.post(callableWrapper);
		
		while(!callableWrapper.isFinished()) {
			try { Thread.sleep(sleepingTime); } catch (InterruptedException e) {}
		}
		
		if (callableWrapper.getException() != null) {
			throw callableWrapper.getException();
		}
		
		return callableWrapper.getResult();
	}
	
	/**
	 * Wrap a callable into a Runnable in order to be able to check if it is executed or not.
	 */
	private static class CallableWrapper<T> implements Runnable {
		
		private final Callable<T> callable;
		private volatile T result = null;
		private volatile Exception exception = null;
		private volatile boolean isFinished = false;

		public CallableWrapper(Callable<T> callable) {
			this.callable = callable;
		}

		@Override
		public void run() {
			try {
				this.result = callable.call();
			} catch (Exception e) {
				this.exception = e;
			}
			this.isFinished = true;
		}

		public T getResult() {
			return result;
		}

		public Exception getException() {
			return exception;
		}

		public boolean isFinished() {
			return isFinished;
		}
	}

}
