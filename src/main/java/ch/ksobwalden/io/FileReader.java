package ch.ksobwalden.io;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;

public interface FileReader {
    BufferedInputStream fromFileSystem(Path path) throws FileNotFoundException;

    BufferedInputStream fromUrl(URL path);
}
