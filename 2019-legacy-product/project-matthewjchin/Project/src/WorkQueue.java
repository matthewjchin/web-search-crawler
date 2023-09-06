
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href="https://www.ibm.com/developerworks/library/j-jtp0730/">Java
 *      Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {

	/**
	 * Pool of worker threads that will wait in the background until work is
	 * available.
	 */
	private final PoolWorker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;
	
	/** Logger used in cases for debugging. */
	private static final Logger logger = LogManager.getLogger();
	
	/** The number of pending tasks. */
	private int pendingTasks;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads 
	 * and that threads are waiting in the background
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];
		this.pendingTasks = 0;
		shutdown = false;
		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
		}
	}

	/**
	 * Adds a work request to the queue. A thread will process this request when
	 * available.
	 *
	 * @param r work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable r) {
		synchronized (queue) {
			queue.addLast(r);
			incrementPendingTasks();
			queue.notifyAll();
		}
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 * 
	 * safe to do unsynchronized due to volatile keyword
	 */
	public void shutdown() {
		shutdown = true;
		synchronized (queue) {
			queue.notifyAll();
		}
		logger.debug("Queue has been shut down. ");
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads in the queue
	 */
	public int size() {
		return workers.length;
	}
	
	/**
	 * Increases the number of pending tasks by one that must be waited on.
	 */
	public void incrementPendingTasks() {
		synchronized (queue) {
			pendingTasks++;
			logger.debug("Number of pending tasks now is: ", pendingTasks);
		}
		
	}
	/**
	 * Decreases the number of pending tasks by one that must be waited on;
	 * checks also that the queue no longer has any tasks that are to be waited
	 * on and then notifies all threads when no pending work is remaining.
	 */
	public void decrementPendingTasks() {
		synchronized (queue) {
			pendingTasks--;
			logger.debug("Number of pending tasks now is: ", pendingTasks);
			if (pendingTasks < 0) {
				queue.notifyAll();
			}
		}
	}
	
	/**
	 * Method for threads waiting in queue until all current work completed.
	 * Used in order for work queue to be shut down.
	 */
	public void finish() {
		try {
			synchronized(queue) {
				while (pendingTasks > 0) {
					logger.debug("Awaiting tasks to be finished. ");
					queue.wait();
					logger.debug("Waiting");
				}
			}
		}
		catch (InterruptedException e) {
			logger.debug("Finished all interrupted", e);
		}
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will
	 * remove the work from the queue and run it. If a shutdown is detected, will
	 * exit instead of grabbing new work from the queue. These threads will
	 * continue running in the background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {
			Runnable r = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {
							queue.wait();
						}
						catch (InterruptedException ex) {
							System.err.println("Warning: Work queue interrupted while waiting.");
							Thread.currentThread().interrupt();
						}
					}
					if (shutdown) {
						break;
					}
					else {
						r = queue.removeFirst();
					}
				}

				try {
					r.run();
				}
				catch (RuntimeException e) {
					System.err.println("Warning: Work queue encountered an exception while running.");
				}
			}
		}
	}
}

