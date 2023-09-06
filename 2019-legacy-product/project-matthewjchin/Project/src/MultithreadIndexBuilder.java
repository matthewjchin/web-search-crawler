import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a thread-safe class with respect to the InvertedIndexBuilder class.   
 * 
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.3
 */
public class MultithreadIndexBuilder extends InvertedIndexBuilder {
	
	/**
	 * A logger used for cases of debugging. 
	 */
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * An exclusive use of a WorkQueue of workers for this class. 
	 */
	private final WorkQueue workers;
	
	/**
	 * A Runnable class to parse through
	 */
	private WorkersRun work;
	
	/**
	 * @param file a file argument to be parsed through
	 * @param index a thread-safe inverted index data structure
	 * @param workers a queue of workers
	 */
	public MultithreadIndexBuilder(Path file, MultithreadIndex index, 
			WorkQueue workers) {
		super(index);
		this.workers = new WorkQueue();
		this.setWork(new WorkersRun(file, index));
	}
	
	/**
	 * Helper method that signifies the end of all work needed to be done.
	 * Necessary for shutting down work queue or resetting count for tasks.
	 */
	public void finish() {
		workers.finish();
	}
	
	/**
	 * Shuts down any workers still listed as pending work once all the work 
	 * has been completed via calling finish() method
	 */
	public void shutDownWorkers() {
		finish();
		logger.debug("Shutting down. ");
		workers.shutdown();
	}
	
	/**
	 * Retrieve the WorkersRun thread object being used for the Builder class. 
	 * 
	 * @return the WorkersRun type
	 */
	public WorkersRun getWork() {
		return work;
	}

	/**
	 * Set the WorkersRun object in use to be run for the thread-safe Builder class.
	 * 
	 * @param work the WorkersRun object to be set and used
	 */
	public void setWork(WorkersRun work) {
		this.work = work;
	}

	/**
	 * 
	 * @author Matthew Chin (matthewjchin)
	 *
	 */
	private class WorkersRun implements Runnable {
		
		/** The file to be used of Path type. */
		private Path file;
		
		/** The QueryBuilder in use. */
		private QueryBuilder query;
		
		/**
		 * Constructor for WorkersRun object
		 * 
		 * @param file the path file passed in as argument
		 * @param index the inverted index data structure passed
		 */
		public WorkersRun(Path file, MultithreadIndex index) {
			logger.debug("IndexBuilder created for file: ", file);
			this.file = file;
			this.query = new QueryBuilder(index);
		}
		
		@Override
		public void run() {
			try {
				query.parse(file, true);
			}
			catch (IOException e) {
				logger.warn("Unable to parse {}", file);
				logger.catching(Level.DEBUG, e);
			}
		}
	}
	
}