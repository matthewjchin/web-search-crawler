import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles all of the actions of the InvertedIndex class in a 
 * thread-safe form in terms of dealing with adding and getting words at
 * their respective positions for the data structure. 
 * 
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.3
 */
public class MultithreadIndex extends InvertedIndex {
	
	/**
	 * A ReadWriteLock for exclusive use in this class.
	 */
	private ReadWriteLock lock;
	
	/**
	 * A reader lock exclusively in use for this class. 
	 */
	private ReadWriteLock.ReadLock readLock;
	
	/**
	 * A writer lock exclusively in use for this class. 
	 */
	private ReadWriteLock.WriteLock writeLock;

	/** A logger for use in debugging. */
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Constructor used to initialize a ReadWriteLock
	 */
	public MultithreadIndex() {
		super();
		this.lock = new ReadWriteLock();
	}
	
	/**
	 * Adds an element at a certain position located in a file with respect
	 * to the file path that is located in into the inverted index data structure.
	 * Implementation is with use of a writer lock.
	 */
	@Override
	public void add(String element, Integer position, String path) {
		writeLock.lock();
		try {
			logger.debug("Adding element to structure. ");
			super.add(element, position, path);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Checks with the use of a reader lock if a String key is found in
	 * the inverted index data structure.
	 */
	@Override
	public boolean contains(String element) {
		readLock.lock();
		try {
			logger.debug("Checking if element is in structure. ");
			return super.contains(element);
		}
		finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Checks with use of a reader lock if a String key is matched with the
	 * file path that it is said to be passed in as an argument and if it is
	 * found in the inverted index data structure. 
	 */
	@Override
	public boolean contains(String element, String path) {
		readLock.lock();
		try {
			return super.contains(element, path);
		}
		finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Checks with use of a reader lock if a String key is matched with the
	 * file path and position in file that it is said to be passed in as argument.
	 * Confirms that elements passed in are navigated in the inverted index data structure. 
	 */
	@Override
	public boolean contains(String element, String path, int position) {
		readLock.lock();
		try {
			return super.contains(element, path, position);
		}
		finally {
			readLock.unlock();
		}
	}
	
	@Override
	public int getNumberPositions(String element) {
		readLock.lock();
		try {
			return super.getNumberPositions(element);
		}
		finally {
			readLock.unlock();
		}
	}
	
	@Override
	public Collection<String> getFiles() {
		readLock.lock();
		try {
			return super.getFiles();
		}
		finally {
			readLock.unlock();
		}
	}
	
	@Override
	public Integer getCount(String path) {
		readLock.lock();
		try {
			return super.getCount(path);
		}
		finally {
			readLock.unlock();
		}
	}
	
	@Override
	public Collection<Integer> getPositions(String element, String path) {
		readLock.lock();
		try {
			return super.getPositions(element, path);
		}
		finally {
			readLock.unlock();
		}
	}
	
	@Override
	public void writeIndex(Path path) throws IOException {
		writeLock.lock();
		try {
			super.writeIndex(path);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	@Override
	public void writeWordCount(Path path) throws IOException {
		writeLock.lock();
		try {
			super.writeWordCount(path);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Performs search on a set of String queries and determines if whether or
	 * not an exact search can be performed on the query. If not then a partial 
	 * search is executed instead. Includes use of a reader lock. 
	 */
	@Override
	public ArrayList<QueryResult> search(Set<String> queries, boolean exact) {
		readLock.lock();
		try {
			return super.search(queries, exact);
		}
		finally {
			readLock.lock();
		}
	}
	
	/**
	 * Return the inverted index data structure in String form with use of 
	 * a reader lock. 
	 */
	@Override
	public String toString() {
		readLock.lock();
		try {
			return super.toString();
		}
		finally {
			readLock.unlock();
		}
	}

	/**
	 * Return the lock that is being used for this class.
	 * 
	 * @return the lock that is in use for this class
	 */
	public ReadWriteLock getLock() {
		return lock;
	}

	/**
	 * Set the lock to the value it needs to be set to. 
	 * 
	 * @param lock the lock to be set and/or initialized
	 */
	public void setLock(ReadWriteLock lock) {
		this.lock = lock;
	}
}