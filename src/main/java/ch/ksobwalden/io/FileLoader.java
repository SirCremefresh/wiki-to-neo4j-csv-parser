package ch.ksobwalden.io;

import ch.ksobwalden.common.Globals;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

import static ch.ksobwalden.common.Globals.getWikidataFolder;

public enum FileLoader {
    PLAIN((path) -> {
        try {
            FileInputStream inputStream = new FileInputStream(getWikidataFolder().resolve(path.getPath()).toFile());
            return new BufferedInputStream(inputStream, Globals.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Could not load PLAIN file: " + path, e);
        }
    }),
    BZ2((path) -> {
        try {
            BufferedInputStream bis = PLAIN.load(path);
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            return new BufferedInputStream(input, Globals.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Could not load BZ2 file: " + path, e);
        }
    }),
    REMOTE_BZ2((path) -> {
        try {
            InputStream fin = path.toURL().openStream();
            BufferedInputStream bis = new BufferedInputStream(fin, Globals.DEFAULT_BUFFER_SIZE);
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            return new BufferedInputStream(input, Globals.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Could not load BZ2 file: " + path, e);
        }
    }),
    ;

    private final Function<URI, BufferedInputStream> loader;

    FileLoader(Function<URI, BufferedInputStream> loader) {
        this.loader = loader;
    }

    public BufferedInputStream load(URI path) {
        return loader.apply(path);
    }
}
