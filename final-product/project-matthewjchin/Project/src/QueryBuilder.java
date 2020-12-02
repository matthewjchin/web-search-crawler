import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * This class does partial search via a query file that checks other files
 * for appearances of the partial querying. 
 * 
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.4
 */
public class QueryBuilder {
	
	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;
	
	/** A TreeMap in use for the stemmed word and the QueryResults list */
	private final TreeMap<String, List<InvertedIndex.QueryResult>> results;

	/** An inverted index data structure exclusively used in the QueryBuilder class. */
	private final InvertedIndex invertedIndex;
	
	/** 
	 * Partial Search Constructor of a TreeMap using QueryBuilder structure which 
	 * creates a new TreeMap for results and sets private InvertedIndex as passed indexs
	 * 
	 * @param index the InvertedIndex argument that is being passed into QueryBuilder
	 */
	public QueryBuilder(InvertedIndex index) {
		this.results = new TreeMap<>();
		this.invertedIndex = index;
	}
	
	/**
	 * Parse through the "-query" flag and its respective value(s) in invertedIndex.
	 * Checks if whether or not a exact search can be performed through the file. Called
	 * as helper method to another parse method which checks line by line if an exact
	 * search is to be performed. If not, a partial search is to be performed.  
	 * 
	 * @param file path the directory that is to be read through
	 * @param exactFlag a true-or-false statement if the flag is an exact search or not
	 * @throws IOException thrown if there are no valid files that can be read through
	 */
	public void parse(Path file, boolean exactFlag) throws IOException {
		
		try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String currLine = null;
			while ((currLine = reader.readLine()) != null) {
				parse(currLine, exactFlag);
			}
		}
	}
	
	/**
	 * Parse through the line and check if each word (token) on that line needs 
	 * to be added to the query data structure after getting stemmed and cleaned. 
	 * Checks also if set of queries is empty and whether or not the map has the 
	 * query argument that is being passed. 
	 * 
	 * @param line the String of words to be read through
	 * @param exactFlag true returned if match is found; false otherwise
	 */
	public void parse(String line, boolean exactFlag) {
		
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		TreeSet<String> queries = new TreeSet<String>();
		for(String queryPart : TextParser.parse(line)) {
			queries.add(stemmer.stem(queryPart).toString());
		}
		if (queries.isEmpty()) {
			return;
		}
		String queryString = String.join(" ", queries);
		if (results.containsKey(queryString)) {
			return;
		}
		List<InvertedIndex.QueryResult> listOfResults = invertedIndex.search(queries, exactFlag);
		this.results.put(queryString, listOfResults);
	}
	
	/**
	 * Writes out all search results in pretty JSON format. Argument passes in file path
	 * and calls asQueryResultTreeMap that passes in the structure of results and all of 
	 * the queries associated.
	 * 
	 * @param resultsPath the file path which contents written to JSON 
	 * @throws IOException thrown if there are no valid files that can be read through
	 */
	public void writeQueryToJSON(Path resultsPath) throws IOException {
		SimpleJsonWriter.asQueryResultTreeMap(results, resultsPath);
	}
	
}