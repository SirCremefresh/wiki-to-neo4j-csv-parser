package ch.ksobwalden.io;

import ch.ksobwalden.common.Globals;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.net.URI;
import java.util.function.BiFunction;

import static ch.ksobwalden.common.Globals.getWikidataFolder;

public enum FileLoader {
    PLAIN((path, reader) -> {
        try {
            return reader.fromFileSystem(getWikidataFolder().resolve(path.getPath()));
        } catch (Exception e) {
            throw new RuntimeException("Could not load PLAIN file: " + path, e);
        }
    }),
    BZ2((path, reader) -> {
        try {
            BufferedInputStream bis = reader.fromFileSystem(getWikidataFolder().resolve(path.getPath()));
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            return new BufferedInputStream(input, Globals.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Could not load BZ2 file: " + path, e);
        }
    }),
    REMOTE_BZ2((path, reader) -> {
        try {
            BufferedInputStream bis = reader.fromUrl(path.toURL());
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            return new BufferedInputStream(input, Globals.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Could not load BZ2 file: " + path, e);
        }
    }),
    ;

    private final BiFunction<URI, FileReader, BufferedInputStream> loader;

    private final static FileReader DEFAULT_READER = new FileReaderImpl();

    FileLoader(BiFunction<URI, FileReader, BufferedInputStream> loader) {
        this.loader = loader;
    }

    public BufferedInputStream load(URI path) {
        return load(path, DEFAULT_READER);
    }

    public BufferedInputStream load(URI path, FileReader fileReader) {
        return loader.apply(path, fileReader);
    }
}
