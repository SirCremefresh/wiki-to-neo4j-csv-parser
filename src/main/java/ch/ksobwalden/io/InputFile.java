package ch.ksobwalden.io;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum InputFile {
    EN_WIKI("enwiki-latest-pages-articles.xml", "en_wiki", FileLoader.PLAIN),
    EN_WIKI_BZ2("enwiki-latest-pages-articles.xml.bz2", "en_wiki", FileLoader.BZ2),
    EN_WIKI_BZ2_REMOTE("https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2", "en_wiki", FileLoader.REMOTE_BZ2),
    SIMPLE_WIKI("simplewiki-latest-pages-articles.xml", "simple_wiki", FileLoader.PLAIN),
    SIMPLE_WIKI_BZ2("simplewiki-latest-pages-articles.xml.bz2", "simple_wiki", FileLoader.BZ2),
    SIMPLE_WIKI_BZ2_REMOTE("https://dumps.wikimedia.org/simplewiki/latest/simplewiki-latest-pages-articles.xml.bz2", "simple_wiki", FileLoader.REMOTE_BZ2),
    ;

    private final URI uri;
    private final String outputFolder;
    private final FileLoader fileLoader;

    InputFile(String fileName, String outputFolder, FileLoader fileLoader) {
        this.outputFolder = outputFolder;
        this.fileLoader = fileLoader;
        this.uri = URI.create(fileName);
    }

    public InputStream load() {
        return fileLoader.load(uri);
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    @Override
    public String toString() {
        return "InputFile{" +
                "uri=" + uri +
                ", outputFolder='" + outputFolder + '\'' +
                ", fileLoader=" + fileLoader +
                '}';
    }

    public static InputFile parse(String s) {
        try {
            return InputFile.valueOf(s);
        } catch (IllegalArgumentException e) {
            String enumNames = Arrays.stream(InputFile.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Could not parse InputFile from: " + s + ". Available values: " + enumNames, e);
        }
    }
}
