package ch.ksobwalden;

import ch.ksobwalden.csv.CsvWriter;
import ch.ksobwalden.wikidump.WikiDumpParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

class IntegrationTest {
    @Test
    void shouldProduceCorrectOutput() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream sampleInputFile = classLoader.getResourceAsStream("sample.xml");

        ByteArrayOutputStream pages = new ByteArrayOutputStream();
        ByteArrayOutputStream links = new ByteArrayOutputStream();

        var pageWriter = new CsvWriter(pages);
        var linkWriter = new CsvWriter(links);

        new WikiDumpParser(pageWriter, linkWriter)
                .process(sampleInputFile);

        var realOutputLinks = getLines(links.toString(Charset.defaultCharset()));
        var realOutputPages = getLines(pages.toString(Charset.defaultCharset()));
        var expectedOutputLinks = getLines(getResourceAsString(classLoader, "sample_links.csv"));
        var expectedOutputPages = getLines(getResourceAsString(classLoader, "sample_pages.csv"));

        Assertions.assertThat(realOutputLinks).containsExactlyInAnyOrderElementsOf(expectedOutputLinks);
        Assertions.assertThat(realOutputPages).containsExactlyInAnyOrderElementsOf(expectedOutputPages);
    }

    private List<String> getLines(String input) {
        return input
                .lines()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }

    private String getResourceAsString(ClassLoader classLoader, String name) throws IOException {
        try (var in = classLoader.getResourceAsStream(name)) {
            assert in != null;
            return new String(in.readAllBytes());
        }
    }
}
