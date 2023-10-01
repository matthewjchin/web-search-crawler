import java.util.TreeMap;
import java.util.TreeSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

//import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * InvertedIndex
 *
 * This is the data structure that will be written and searched into
 * in order to retrieve items from the inverted index data structure. 
 *
 * @author Matthew Chin (matthewjchin)
 * @version Fall 2019
 * @version v3.0.4
 */
public class InvertedIndex {

    /** Default use of a Snowball Stemmer */
//    public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

    /**
     * Use of a QueryResult class that uses Comparable and resources in InveredIndex class
     *
     * @author Matthew Chin (matthewjchin)
     * @version Fall 2019
     */
    public class QueryResult implements Comparable<QueryResult> {

        /** The name of the file path being read. */
        private final String file;

        /** The number of times a word being searched is found in the file. */
        private int appearances;

        /** The score percentage of the number of times word is in file divided by word count in file. */
        private double score;

        /**
         * Constructor for QueryResults which initializes the word count at 1
         * for file and the number of appearances of a word in file at 0. Also
         * initializes the global file being as the file in use for each iteration.
         *
         * @param file the file to be queried through
         */
        public QueryResult(String file) {
            this.file = file;
            this.appearances = 0;
        }

        /**
         * Retrieve the path or the name of the file the word is associated with.
         *
         * @return the name of the file
         */
        public String getPathFile() {
            return file;
        }

        /**
         * Get the score of a word featured in the file. The word's score
         * can be found by getting the number of appearances the word is featured
         * in that file divided by the word count of that file.
         * This will result in a decimal showing the percentage to the hundred-millionth
         * place, or eight decimals back.
         *
         * @return the percentage of the number of times word appears in file versus total words
         */
        public String getWordScore() {
            DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
            return FORMATTER.format(this.score);
        }

        /**
         * Update the number of appearances of word featured in file.
         * Passes in the word in a file that appears.
         *
         * @param word the argument passed in to check in the file
         * @param location the location for the key.
         */
        private void updateAppearances(String word, String location) {
            this.appearances += nestedMap.get(word).get(location).size();
            this.score = (double) appearances / wordCountMap.get(location);
        }

        /**
         * Retrieve the number of appearances a word is found in a file.
         *
         * @return the number of appearances word is found in file
         */
        public int getAppearances() {
            return this.appearances;
        }

        /**
         * Compares the number of appearances a word is found in a file in comparison to the
         * actual number of appearances that word is found in that file. Comparison done in order
         * to return the actual value of appearances of a word in that file.
         *
         * @param other the QueryResult in use for comparison
         *
         * @return the value of the String query path and if there is a match
         */
        public int compareTo(QueryResult other) {

            int scoreCompare = Double.compare(other.score, this.score);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            int matchCompare = Integer.compare(other.getAppearances(), this.getAppearances());
            return matchCompare != 0 ?
                    matchCompare : this.getPathFile().compareTo(other.getPathFile());
        }

        @Override
        public String toString() {
            return "File Name: " + file
                    + "Word Count: " + wordCountMap.get(file) + "Position: " + appearances;
        }
    }

    /**
     * A nested TreeMap with reference of a Key, which is a word to be parsed
     * A Value is a map of a key that is the path of the file the word is found in and
     * a value that has the number(s) of occurrences of the positions word found in file
     */
    private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> nestedMap;

    /** This is a separate data structure for word count of files and their respective path location  */
    private final TreeMap<String, Integer> wordCountMap;

    /** Constructor of InvertedIndex class which contains TreeMaps for data structure for words and file word counts */
    public InvertedIndex() {
        this.nestedMap = new TreeMap<>();
        this.wordCountMap = new TreeMap<>();
    }

    /**
     * Adds the element and position with respect to the file that it is located in.
     * Arguments that are passed in are the word, the position(s) associated with that
     * word in the file, and the path file itself that it is associated in.
     *
     * @param element  the element that is in question of being added
     * @param position the position the element was found
     * @param path the directory path to file that searched through as String
     */
    public void add(String element, Integer position, String path) {
        nestedMap.putIfAbsent(element, new TreeMap<String, TreeSet<Integer>>());
        nestedMap.get(element).putIfAbsent(path, new TreeSet<Integer>());
        if (nestedMap.get(element).get(path).add(position)) {
            wordCountMap.put(path, wordCountMap.getOrDefault(path, 0) + 1);
        }
    }

    /**
     * Determines whether the element is stored in the index.
     *
     * @param element the element to look up
     * @return {@true} if the word is stored in the index; false otherwise
     */
    public boolean contains(String element) {
        return nestedMap.containsKey(element);
    }

    /**
     * Checks if element key in index contains or is associated with the file path
     *
     * @param element the element to be looked up
     * @param path the directory path with file that is to be searched through
     * @return {@true} if word is found and file path associated to word are found; false otherwise
     */
    public boolean contains(String element, String path) {
        return contains(element) && nestedMap.get(element).containsKey(path);
    }

    /**
     * Determines whether the element is stored in the index and the position is
     * stored for that element.
     *
     * @param element  the argument that is in question to this method
     * @param path the directory path that is being searched through
     * @param position the position of that element to lookup
     * @return {@true} if the element and position is stored in the index; false otherwise
     */
    public boolean contains(String element, String path, int position) {
        return contains(element, path) && nestedMap.get(element).get(path).contains(position);
    }

    /**
     * Returns the number of positions stored for the given element.
     *
     * @param element the element to lookup
     * @return the number of positions stored for that element
     */
    public int getNumberPositions(String element) {
        return contains(element) ? nestedMap.get(element).size() : 0;
    }

    /**
     * Outputs the number of elements in InvertedIndex data structure.
     *
     * @return the number of elements in index if structure is filled; 0 if index is empty
     */
    public int numberOfElementsInStructure() {
        return nestedMap.size();
    }

    /**
     * Return the String keywords in the nestedMap data structure
     * and checks if that set is not modified
     *
     * @return a Collection of keys featured in the structure
     */
    public Collection<String> getWords() {
        return Collections.unmodifiableSet(nestedMap.keySet());
    }

    /**
     * Check if word exists in index and return the keySet of that word's inner map
     * via a String Collection of keys and checks if that set is not modified
     *
     * @param word the word to be retrieved from the index
     * @return a Collection of Strings that is a keySet respective of that word
     */
    public Collection<String> getLocations(String word) {
        return contains(word) ?
                Collections.unmodifiableCollection(nestedMap.get(word).keySet()) :
                Collections.emptySet();
    }

    /**
     * Check if the path to a file in String form is in a wordCountMap structure
     * and validates if that path is found in Collection of Strings
     *
     * @return a Collection of Strings that contains all the file paths
     */
    public Collection<String> getFiles() {
        return Collections.unmodifiableCollection(wordCountMap.keySet());
    }

    /**
     * Check if the associated file path in wordCountMap structure contains an
     * Integer value and validates if the associated count of words matches what
     * has the number of stems in that file
     *
     * @param path the path to be checked if there are values associated with it
     * @return a Collection of Integers that contains all the file paths' word counts
     */
    public Integer getCount(String path) {
        return wordCountMap.getOrDefault(path, 0);
    }

    /**
     * Outputs an unmodifiable view of the position(s) stored in the index for the
     * provided element. These are the position(s) that the element are/can be founded in.
     *
     * @param element the string keyword featured in file
     * @param path the directory path to be searched through in String type
     * @return the position of the element with respect to file located in; empty Collection otherwise
     */
    public Collection<Integer> getPositions(String element, String path) {
        return contains(element, path) ?
                Collections.unmodifiableCollection(nestedMap.get(element).get(path)) :
                Collections.emptySet();
    }

    /**
     * Write to the InvertedIndex data structure that consists of the key word in String form,
     * the file path(s) that word is featured, also in String form, and an ArrayList of Integers that
     * signal the indices of where that word appears in that file.
     *
     * @param path The directory path in String form to be searched through in order to write to structure
     * @throws IOException thrown in case a file cannot be read through or if invalid input given
     */
    public void writeIndex(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            SimpleJsonWriter.asBigNestedObject(nestedMap, writer, 0);
        }
    }

    /**
     * Writes to the wordCount data structure that consists of a file path in String form and the number of
     * words/stems that file contains in Integer form.
     *
     * @param path the directory path in Path form to be searched through in order to write to structure
     * @throws IOException thrown in case a file cannot be read through or if invalid input given
     */
    public void writeWordCount(Path path) throws IOException {
        SimpleJsonWriter.asObject(wordCountMap, path, 0);
    }

    /**
     * This is a helper searching method that determines whether or not the set of
     * queries should be parsed through with an exact search or with a partial search.
     * This can be determined by the boolean passed into this method that determines
     * if the exactSearch() method can be used.
     *
     * @param queries the queries to be searched with or at
     * @param exact a boolean that determines if the query matches the stem argument
     * @return an ArrayList of all query results that are to be searched
     */
    public ArrayList<QueryResult> search(Set<String> queries, boolean exact) {
        return exact ? exactSearch(queries) : partialSearch(queries);
    }

    /**
     * Method returns exact search results from inverted index where any word stem
     * in the index exactly matches the query word searched for and returned.
     *
     * @param queries all search queries that are exact matches with search results
     * @return an ArrayList of all query results
     */
    public ArrayList<QueryResult> exactSearch(Set<String> queries) {

        HashMap<String, QueryResult> lookup = new HashMap<String, QueryResult>();
        ArrayList<QueryResult> listResults = new ArrayList<QueryResult>();
        for(String oneQuery : queries) {
            if (nestedMap.containsKey(oneQuery)) {
                searchHelper(listResults, lookup, oneQuery);
            }
        }
        Collections.sort(listResults);
        return listResults;
    }

    /**
     * Method does a partial search on the inverted index data structure.
     * This method is called if an exact search for queries cannot be done.
     *
     * @param queries a TreeSet structure to do partialSearch
     * @return a list of Query Results
     */
    public ArrayList<QueryResult> partialSearch(Set<String> queries) {

        HashMap<String, QueryResult> lookup = new HashMap<String, QueryResult>();
        ArrayList<QueryResult> listResults = new ArrayList<QueryResult>();
        for(String oneQuery : queries) {
            for(String stem : this.nestedMap.tailMap(oneQuery).keySet()) {
                if (!stem.startsWith(oneQuery)) {
                    break;
                }
                searchHelper(listResults, lookup, stem);
            }
        }
        Collections.sort(listResults);
        return listResults;
    }

    /**
     * Helper method exclusively used for partialSearch method such that
     * the first possible key in a list can be found and linked to the
     * associated query.
     *
     * @param list the collection of queries to be searched through
     * @param lookup the HashMap structure in connection with the word and its query
     * @param word the word to be searched for in question
     */
    private void searchHelper(ArrayList<QueryResult> list,
                              HashMap<String, QueryResult> lookup, String word) {

        for(String location : this.nestedMap.get(word).keySet()) {
            if(lookup.containsKey(location) == false) {
                QueryResult result = new QueryResult(location);
                lookup.put(location, result);
                list.add(result);
            }
            lookup.get(location).updateAppearances(word, location);
        }
    }

    /**
     * Retrieve the Default use of a SnowballStemmer Object exclusively for this class.
     *
     * @return the Default use of a Snowball Stemmer Object
     */
    public static SnowballStemmer.ALGORITHM getDefault() {
        return DEFAULT;
    }

    @Override
    public String toString() {
        return nestedMap.toString();
    }
}