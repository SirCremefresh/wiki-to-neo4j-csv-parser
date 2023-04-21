package ch.ksobwalden.io;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class FileLoaderTest {

    @Test
    void loadPlainFile() throws IOException {
        var content = "hello world";
        FileReader fakeReader = createFileReaderFor(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        var resultStream = FileLoader.PLAIN.load(URI.create("/test"), fakeReader);
        var result = IOUtils.toString(resultStream, StandardCharsets.UTF_8);

        Assertions.assertThat(result).isEqualTo(content);
    }

    @Test
    void loadBz2File() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream uncompressedFileStream = classLoader.getResourceAsStream("sample.xml");
        assert uncompressedFileStream != null;
        var uncompressedFile = IOUtils.toString(uncompressedFileStream, StandardCharsets.UTF_8);
        InputStream compressedFile = classLoader.getResourceAsStream("sample.xml.bz2");

        FileReader fakeReader = createFileReaderFor(compressedFile);

        var resultStream = FileLoader.BZ2.load(URI.create("/test"), fakeReader);
        var result = IOUtils.toString(resultStream, StandardCharsets.UTF_8);

        Assertions.assertThat(result).isEqualTo(uncompressedFile);
    }

    private FileReader createFileReaderFor(InputStream content) {
        return new FileReader() {
            @Override
            public BufferedInputStream fromFileSystem(Path path) {
                return new BufferedInputStream(content);
            }

            @Override
            public BufferedInputStream fromUrl(URL path) {
                return new BufferedInputStream(content);
            }
        };
    }
}
