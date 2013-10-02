package org.opentravelmate.commons;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * Execute the given Runnable when the executor is ready.
 * 
 * @author Marc Plouhinec
 */
public class OnReadyExecutor implements Executor {
	
	private Queue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
	private volatile boolean isReady = false;

	@Override
	public void execute(Runnable task) {
		if (isReady) {
			task.run();
		} else {
			tasks.add(task);
		}
	}

	/**
	 * @return true if the executor is ready, false if not.
	 */
	public boolean isReady() {
		return isReady;
	}

	/**
	 * Tell the executor the ready status.
	 * If ready, all the stored Runnable are executed.
	 * 
	 * @param isReady
	 */
	public void setReady(boolean isReady) {
		this.isReady = isReady;
		
		if (isReady) {
			while (!tasks.isEmpty()) {
				tasks.poll().run();
			}
		}
	}
}
