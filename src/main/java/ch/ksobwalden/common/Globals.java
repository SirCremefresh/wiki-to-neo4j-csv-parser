package ch.ksobwalden.common;

import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Globals {
    public static final int DEFAULT_BUFFER_SIZE = 1048576;
    private static final String WIKIDATA_FOLDER = "wikidata";

    public static Path getWikidataFolder() {
        var folder = FileUtils
                .getUserDirectory().toPath()
                .resolve(WIKIDATA_FOLDER);
        if (Files.notExists(folder)) {
            throw new RuntimeException("Wikidata folder does not exist at path: " + folder);
        }
        return folder;
    }
}
