package ch.ksobwalden.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class OutputFileStore {
    private static final int MB256_IN_BYTES = 268435456;
    private static final String LINKS_CSV = "links-";
    private static final String PAGES_CSV = "pages-";
    private static final String CSV_FILE_ENDING = ".csv";

    public static OutputFiles getOutputFilesForInput(InputFile inputFile) {
        Path outputDirectory = Paths.get("data", inputFile.getOutputFolder()).toAbsolutePath();
        createOutputDirectoryIfNotExists(outputDirectory);
        assertOutputEmpty(outputDirectory);

        return new OutputFiles(
                new SplitFileOutputStream(outputDirectory, PAGES_CSV, CSV_FILE_ENDING, MB256_IN_BYTES),
                new SplitFileOutputStream(outputDirectory, LINKS_CSV, CSV_FILE_ENDING, MB256_IN_BYTES)
        );
    }

    private static void assertOutputEmpty(Path outputDirectory) {
        if (Objects.requireNonNull(outputDirectory.toFile().list()).length != 0) {
            throw new RuntimeException("""
                    Files already exist in the output folder: %s
                    Please remove all files from it
                    """.formatted(outputDirectory));
        }
    }

    private static void createOutputDirectoryIfNotExists(Path outputDirectory) {
        if (Files.notExists(outputDirectory)) {
            try {
                Files.createDirectories(outputDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Could not create output directory at: " + outputDirectory.toAbsolutePath(), e);
            }
        }
    }
}
