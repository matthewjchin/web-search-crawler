import java.util.ConcurrentModificationException;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive, but
 * also tracks which thread holds the lock. If unlock is called by any other
 * thread, a {@link ConcurrentModificationException} is thrown.
 *
 * @see SimpleLock
 * @see ReadWriteLock
 */
public class ReadWriteLock {

	/** A lock object for this class. */
	public Object lock;
	
	/** The number of readers exclusively used for this class. */
	private int readers;
	
	/** The number of writers exclusively for this class. */
	private int writers;
	
	
	/**
	 * Initializes a new simple read/write lock.
	 */
	public ReadWriteLock() {
		this.writers = 0;
		this.readers = 0;
	}
	
	
	/**
	 * Determines whether the thread running this code and the other thread are
	 * in fact the same thread.
	 *
	 * @param other the other thread to compare
	 * @return true if the thread running this code and the other thread are not
	 * null and have the same ID
	 *
	 * @see Thread#getId()
	 * @see Thread#currentThread()
	 */
	public static boolean sameThread(Thread other) {
		return other != null && other.getId() == Thread.currentThread().getId();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	public class ReadLock implements SimpleLock {

		/**
		 * Will wait until there are no active writers in the system, and then will
		 * increase the number of active readers.
		 */
		@Override
		public void lock() {
			
			synchronized(lock) {
				try {
					while (writers > 0) {
						this.wait();
					}
				} catch (InterruptedException e) {
					System.out.println("Cannot be done. ");
					
				}
			}
			readers++;
		}
		

		/**
		 * Will decrease the number of active readers, and notify any waiting threads if
		 * necessary.
		 */
		@Override
		public void unlock() {
			synchronized(lock) {
				
				try {
					readers--;
					if (readers <= 0) {
						lock.notifyAll();
					}
				} catch (Exception e) {
					System.out.println("Unlocking cannot be done with: " +
							Thread.currentThread().toString());
				}
				
			}
		}

	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	public class WriteLock implements SimpleLock {

		/**
		 * Will wait until there are no active readers or writers in the system, and
		 * then will increase the number of active writers and update which thread
		 * holds the write lock.
		 */
		@Override
		public void lock() {
			synchronized(lock) {
				
				while (readers > 0 || writers > 0) {
					try {
						this.wait();
					} catch (Exception e) {
						System.out.println("Locking writer cannot be done with: " + 
								Thread.currentThread().toString());
					}
				}
				writers++;
			}
		}

		/**
		 * Will decrease the number of active writers, and notify any waiting threads if
		 * necessary. If unlock is called by a thread that does not hold the lock, then
		 * a {@link ConcurrentModificationException} is thrown.
		 *
		 * @see #sameThread(Thread)
		 *
		 * @throws ConcurrentModificationException if unlock is called without previously
		 * calling lock or if unlock is called by a thread that does not hold the write lock
		 */
		@Override
		public void unlock() throws ConcurrentModificationException {
			synchronized(lock) {
				try {
					if (Thread.currentThread().isInterrupted()) {
						throw new ConcurrentModificationException();
					}
					writers--;
					lock.notifyAll();
					
				} catch (ConcurrentModificationException e) { 
					System.out.println("Wrong thread is locked or unlocked");
				}
			}
		}
	}

	
}