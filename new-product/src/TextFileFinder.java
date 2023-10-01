import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for finding all text files in a directory using lambda
 * functions and streams. Verifies that both directories and text files 
 * are indeed what they are when being parsed through. 
 *
 * @author Matthew Chin (matthewjchin)
 * @version v1.0.0
 */
public class TextFileFinder {

    /**
     * Lambda function checks if a file ends in .txt or .text extension
     * (case-insensitive). Useful for {@link Files#walk(Path, FileVisitOption...)}.
     *
     * Returns true if file ends in one of the extensions; false otherwise
     *
     * @see Files#isRegularFile(Path, java.nio.file.LinkOption...)
     * @see Path#getFileName()
     * @see Files#walk(Path, FileVisitOption...)
     */
    public static final Predicate<Path> IS_TEXT = (path) -> {
        String lower = path.toString().toLowerCase();
        return Files.isRegularFile(path) && (lower.endsWith(".txt") || lower.endsWith(".text"));
    };

    /**
     * A lambda function that returns true if the path is a file that ends in a .txt or .text extension
     * (case-insensitive). Useful for {@link Files#find(Path, int, BiPredicate, FileVisitOption...)}.
     *
     * @see Files#find(Path, int, BiPredicate, FileVisitOption...)
     */
    public static final BiPredicate<Path, BasicFileAttributes> IS_TEXT_ATTR = (path, attr) -> IS_TEXT.test(path);

    /**
     * Returns a stream of text files, following any symbolic links encountered.
     *
     * @param start the initial path to start with
     * @return a stream of text files
     *
     * @throws IOException thrown in case invalid input provided/output provided or returned
     *
     * @see #IS_TEXT
     *
     * @see FileVisitOption#FOLLOW_LINKS
     * @see Files#walk(Path, FileVisitOption...)
     * @see Files#find(Path, int, java.util.function.BiPredicate, FileVisitOption...)
     *
     * @see Integer#MAX_VALUE
     */
    public static Stream<Path> find(Path start) throws IOException {
        return Files.walk(start, FileVisitOption.FOLLOW_LINKS).filter(IS_TEXT);
    }

    /**
     * Returns a list of text files.
     *
     * @param start the initial path to search
     * @return list of text files
     *
     * @throws IOException thrown in case invalid input provided/output provided or returned
     *
     * @see #find(Path)
     */
    public static List<Path> list(Path start) throws IOException {
        return find(start).collect(Collectors.toList());
    }
}