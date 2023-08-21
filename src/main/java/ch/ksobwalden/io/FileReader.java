package ch.ksobwalden.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public interface FileReader {
    BufferedInputStream fromFileSystem(Path path) throws IOException;

    BufferedInputStream fromUrl(URL path) throws IOException;
}
