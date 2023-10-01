import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * InvertedIndexBuilder
 *
 * Class does word stemming, formatting, and building the inverted index data structure
 * prior to being able to write into it.
 *
 * @author Matthew Chin (matthewjchin)
 * @version v1.0.0
 */
public class InvertedIndexBuilder {

    /** The default stemmer algorithm used by this class. */
    public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

    /** A separate InvertedIndex data structure for use in the Builder class. */
    private final InvertedIndex index;

    /**
     * InvertedIndexBuilder constructor passes argument of InvertedIndex to be
     * used in comparison with the private InvertedIndex created in Builder class
     *
     * @param index the InvertedIndex structure passed from InvertedIndex class
     */
    public InvertedIndexBuilder(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Creates InvertedIndex structure from the path that is taken.
     * Adds the file to the structure as a result of calling addFile.
     * Iterates through all the files and checks for one on path before calling addFile.
     *
     * @param inputPath the path that is checked
     * @throws IOException thrown in case a file cannot be read through or if invalid input
     */
    public void buildIndexFromPath(Path inputPath) throws IOException {
        for (Path file : TextFileFinder.list(inputPath)) {
            addFile(file);
        }
    }

    /**
     * Helper method for addFile(Path file, InvertedIndex index)
     *
     * @param file the path that is being searched through
     * @throws IOException thrown in case a file cannot be read through or if invalid input
     */
    public void addFile(Path file) throws IOException {
        addFile(file, this.index);
    }

    /**
     * Builds inverted index data structure once contents of file
     * are read through, parsed, and stemmed for searching.
     *
     * Also adds to the wordCountMap data structure that takes a path and counts
     * the total number of word stems found in that file within the directory path
     * that it is located in.
     *
     * @param file the path that is to be searched through
     * @param index the InvertedIndex data structure used for Builder class
     * @throws IOException thrown in case a file cannot be read through or if invalid input
     */
    public static void addFile(Path file, InvertedIndex index) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Stemmer stemmer = new SnowballStemmer(DEFAULT);
            Integer counter = 1;
            String fileString = file.toString();
            String currLine = null;
            while ((currLine = reader.readLine()) != null) {
                for (String word : TextParser.parse(currLine)) {
                    index.add(stemmer.stem(word).toString(), counter++, fileString);
                }
            }
        }
    }
}
