package ch.ksobwalden.io;

import ch.ksobwalden.common.Globals;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public class FileReaderImpl implements FileReader {
    @Override
    public BufferedInputStream fromFileSystem(Path path) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(path.toFile());
        return new BufferedInputStream(inputStream, Globals.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public BufferedInputStream fromUrl(URL path) {
        InputStream a= null;
        try {
            a = path.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new BufferedInputStream(a, Globals.DEFAULT_BUFFER_SIZE);
    }
}
