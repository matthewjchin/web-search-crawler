import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.String;

/**
 * Outputs several simple data structures in "pretty" JSON format where
 * newlines are used to separate elements and nested elements are indented.
 *
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.4
 */
public class SimpleJsonWriter {
	
	/**
	 * Writes the elements as a pretty JSON array. 
	 * Uses iterator to parse through collection and write to JSON file. 
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asArray(Collection<Integer> elements, Writer writer,
		int level) throws IOException {
		
		var itr = elements.iterator();
		writer.append("[\n");
		level++;
		if(itr.hasNext()) {
			indent(itr.next(), writer, level);
		}
		while(itr.hasNext()) {
			writer.write(",\n");
			indent(itr.next(), writer, level);
		}
		writer.write("\n");
		indent("]", writer, level - 1);
	}
	
	/**
	 * Writes the elements as a pretty JSON array to file.
	 * Helper method that passes a BufferedWriter object to asArray
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException thrown in case invalid input provided/output returned
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}
	
	/**
	 * Returns the elements as a pretty JSON array. Helper method that writes all
	 * output to a file in pretty JSON format.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Iterate through the map's keys and values.
	 * Parse each String Entry of the Map which will parse through Map entrySet
	 * End all formatting and break out of for loop once end of entrySet reached
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		writer.write("{\n");
		var iterator = elements.entrySet().iterator();
		level++;
		if (iterator.hasNext()) {
			var entry = iterator.next();
			quote(entry.getKey(), writer, level);
			writer.write(": " + entry.getValue());
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			var entry = iterator.next();
			quote(entry.getKey(), writer, level);
			writer.write(": " + entry.getValue());
		}
		writer.write('\n');
		indent("}", writer, level - 1);
	}
	
	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @param path the file path to use
	 * @param level the counting variable in use during iterations 
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String asObject(Map<String, Integer> elements, Path path, Integer level) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			StringWriter strWriter = new StringWriter();
			asObject(elements, writer, level);
			return strWriter.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object. The generic notation used
	 * allows this method to be used for any type of map with any type of nested
	 * collection of integer objects.
	 * 
	 * Break out of for loop once last Integer value in HashSet has been reached
	 * Parse through every integer value associated with element in keySet
	 * Break out of outer for-each loop if last element in keySet reached
	 * 
	 * @param elements the collection to be searched through
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>> elements, Writer writer, 
			int level) throws IOException {
		
		writer.write("{\n");
		var iterator = elements.entrySet().iterator();
		level++;
		if (iterator.hasNext()) {
			var indivItr = iterator.next();
			quote(indivItr.getKey().toString(), writer, level);
			writer.write(": ");
			asArray(indivItr.getValue(), writer, level);
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			var indivItr = iterator.next();
			quote(indivItr.getKey().toString(), writer, level);
			writer.write(": ");
			asArray(indivItr.getValue(), writer, level);
		}
		writer.write('\n');
		indent("}", writer, level - 1);
	}
	
	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static String asNestedObject(Map<String, ? extends Collection<Integer>> elements, 
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedObject(elements, writer, 0);
			StringWriter strWriter = new StringWriter();
			return strWriter.toString();
		}
	}
	
	/**
	 * Writes all stemmed keywords and respective values (paths and indices) into one large
	 * inverted index data structure
	 * 
	 * Calls asNestedObject method which in turn calls asArray function to write to structure
	 * 
	 * @param elements the data structure of keywords and respective appearances in files
	 * @param writer the BufferedWriter being used to write to inverted index data structure
	 * @param level the count at which keys and values are being entered into structure
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asBigNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, 
			Writer writer, int level) throws IOException {
		
		writer.write("{\n");
		level++;
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var nestIterator = iterator.next();
			quote(nestIterator.getKey().toString(), writer, level);
			writer.write(": ");
			asNestedObject(nestIterator.getValue(), writer, level);
		}
		while (iterator.hasNext()) {
			var entry = iterator.next();
			writer.write(",\n");
			quote(entry.getKey().toString(), writer, level);
			writer.append(": ");
			asNestedObject(entry.getValue(), writer, level);
		}
		writer.write("\n");
		indent("}\n", writer, level - 1);
	}
	
	/**
	 * Supports queries and writes all such into a map data structure. 
	 *
	 * @param queries the structure of words and associated queries
	 * @param path the file that is used and is in question
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asQueryResultMap(TreeMap<String, List<InvertedIndex.QueryResult>> queries, Path path) throws IOException {
		
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			Iterator<String> iterator = queries.keySet().iterator();
			writer.append("{\n");
			if(iterator.hasNext()) {
				String query = iterator.next();
				quote(query, writer, 1);
				writer.write(": [\n");
				queryResultListWriter(queries.get(query), writer);
			}
			while(iterator.hasNext()) {
				String query = iterator.next();
				indent("],\n", writer, 1);
				quote(query, writer, 1);
				writer.write(": [\n");
				queryResultListWriter(queries.get(query), writer);
			}
			indent("]\n", writer, 1);
			writer.append("}\n");
		}
	}
	
	/**
	 * Helper method that calls asQueryResultMap(TreeMap<String, List<QueryResult>> queries, Path path)
	 * by taking in InvertedIndex structure and a path associated with it. 
	 * 
	 * @param results the TreeMap of search results
	 * @param resultsPath the file path of all results
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void asQueryResultTreeMap(TreeMap<String, List<InvertedIndex.QueryResult>> results,
			Path resultsPath) throws IOException {
		asQueryResultMap(results, resultsPath);
	}

	/**
	 * Writes all searching results to pretty JSON format. Done after search has been performed
	 * on element from the Inverted Index data structure.
	 * 
	 * @param result a set of file, word count, and word score associated with word
	 * @param writer the BufferedWriter in use
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	private static void writeResult(InvertedIndex.QueryResult result, Writer writer) throws IOException {
		
		quote("where", writer, 3);
		writer.write(": ");
		quote(result.getPathFile(), writer);
		writer.write(",\n");
		quote("count", writer, 3);
		writer.write(": ");
		writer.write(Integer.toString(result.getAppearances()));
		writer.write(",\n");
		quote("score", writer, 3);
		writer.write(": ");
		writer.write(result.getWordScore());
		writer.write("\n");
	}

	/**
	 * Writes everything associated with search results in pretty JSON format: 
	 * the word, everything in writeResult methods
	 * 
	 * @param query the results being written in pretty JSON format
	 * @param writer the BufferedWriter in use
	 * @throws IOException thrown in case invalid input provided/output returned
	 * 
	 * @see #writeResult(InvertedIndex.QueryResult, Writer)
	 */
	public static void queryResultListWriter(List<InvertedIndex.QueryResult> query, Writer writer) throws IOException {
		
		if(!query.isEmpty()) {
			var iter = query.iterator();
			if(iter.hasNext()) {
				indent("{\n", writer, 2);
				writeResult(iter.next(), writer);
			}
			while(iter.hasNext()) {
				indent("},\n", writer, 2);
				indent("{\n", writer, 2);
				writeResult(iter.next(), writer);
			}
			indent("}\n", writer, 2);
		}
	}

	
	/**
	 * Formats entries from Map of elements using {@code \n} character, then writes the key
	 * and the value(s) associated with key with separation via a colon and a space 
	 * 
	 * @param entry String key and Integer value pair 
	 * @param writer the BufferedWriter in use to write to inverted index data structure in JSON format
	 * @param level index count of iterations
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void writeEntry(Entry<String, Integer> entry, Writer writer, Integer level) 
			throws IOException {
		writer.write('\n');
		quote(entry.getKey().toString(), writer, level);
		writer.write(": ");
		writer.write(entry.getValue().toString());
	}
	
	/**
	 * Formats entries from an Entry of TreeMap. This Entry has a key of String type and its value 
	 * contains an Entry that in itself a String value and an Integer value. 
	 * 
	 * @param entry a Collection of Integers corresponding to a word featured in a file
	 * @param writer the BufferedWriter in use to write to inverted index data structure in JSON format
	 * @param level index of the count of iterations
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void writeNestedEntry(Collection<Integer> entry, Writer writer, 
			Integer level) throws IOException {
		
		writer.write("[\n");
		level++;
		var iterator = entry.iterator();
		if (iterator.hasNext()) {
			var pathItr = iterator.next();
			indent(pathItr.toString(), writer, level);
		}
		while (iterator.hasNext()) {
			var pathItr = iterator.next();
			writer.write(",\n");
			indent(pathItr.toString(), writer, level);
		}
		writer.write('\n');
		indent("]", writer, level - 1);
	}
	
	/**
	 * Writes the {@code \t} tab symbol by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException thrown in case invalid input provided/output returned
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException thrown in case invalid input provided/output returned
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException thrown in case invalid input provided/output returned
	 */
	public static void quote(String element, Writer writer) throws IOException {
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException thrown in case invalid input provided/output returned
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		quote(element, writer);
	}

}