import java.nio.file.Path;
import java.io.IOException;

/**
 * The main class being used to check for valid and invalid flags
 * This is the first checkpoint used to determine valid and invalid input from user
 * 
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.4
 */
public class Driver {
	
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or 
	 * search an inverted index. Also checks for other flags which include
	 * queries/searches and the results that may or not be associated with 
	 * the queries. 
	 *
	 * @param args flag-value pairs used to start this program
	 */
	public static void main(String[] args) {
		
		ArgumentParser argParser = new ArgumentParser(args);
		InvertedIndex invertedIndex = new InvertedIndex();
		InvertedIndexBuilder builder = new InvertedIndexBuilder(invertedIndex);
		QueryBuilder queryBuilder = new QueryBuilder(invertedIndex);
		MultithreadIndex threadIndex = new MultithreadIndex();
		
		if (argParser.hasFlag("-path")) {
			
			Path getPath = argParser.getPath("-path");
			if (getPath != null) {
				try {
					builder.buildIndexFromPath(getPath);
				}
				catch (IOException e) {
					System.out.println("Unable to read file(s) from path: " + getPath.toString());
				}
			}
			else {
				System.out.println("Please give a valid path or directory. ");
			}
		}
		if (argParser.hasFlag("-index")) {
			
			Path indexPath = argParser.getPath("-index", Path.of("index.json"));
			if (indexPath != null) {
				try {
					invertedIndex.writeIndex(indexPath);
				}
				catch (IOException e) { 
					System.out.println("Unable to write to JSON file output at: " + indexPath.toString());
				}
			}
			else {
				System.out.println("Invalid path for JSON file output. ");
			}
		}
		if (argParser.hasFlag("-counts")) {
			
			Path getPath = argParser.getPath("-counts", Path.of("counts.json"));
			if (getPath != null) {
				try {
					invertedIndex.writeWordCount(getPath);
				}
				catch (IOException e) {
					System.out.println("Unable to write word count to JSON file output at: " + getPath.toString());
				}
			}
			else {
				System.out.println("Invalid path for JSON file output. ");
			}
		}
		if (argParser.hasFlag("-query")) {
			
			Path queryPath = argParser.getPath("-query");
			if (queryPath != null) {
				try {
					queryBuilder.parse(queryPath, argParser.hasFlag("-exact"));
				}
				catch (IOException e) {
					System.out.println("Unable to write query results at: " + queryPath.toString());
				}
			}
			else {
				System.out.println("Error, unable to check for a query or a search. ");
			}
		}
		if (argParser.hasFlag("-results")) {
			
			Path resultsPath = argParser.getPath("-results", Path.of("results.json"));
			if (resultsPath != null) {
				try {
					queryBuilder.writeQueryToJSON(resultsPath);
				}
				catch(IOException e) {
					System.out.println("Error, cannot write at: " + resultsPath.toString());
				}
			}
			else {
				System.out.println("Error, cannot check nor write for results to JSON format. ");
			}
		}
		if (argParser.hasFlag("-threads")) {
			
			Path thread = argParser.getPath("-threads");
			int numThreads;
			if (thread != null) {
				try {
					numThreads = Integer.parseInt(thread.toString());
					System.out.println("hi " + thread.toString());
				}
				catch (NumberFormatException e) {
					numThreads = 5;
				}
				if (numThreads > 0) {
					WorkQueue workers = new WorkQueue(numThreads);
					MultithreadIndexBuilder threadBuilder = 
							new MultithreadIndexBuilder(thread, threadIndex, workers);
					threadBuilder.shutDownWorkers();
					workers.shutdown();
				}
			}
			else {
				System.out.println("Unable to make threads with thread");
				return;
			}
		}
		
	}
}
