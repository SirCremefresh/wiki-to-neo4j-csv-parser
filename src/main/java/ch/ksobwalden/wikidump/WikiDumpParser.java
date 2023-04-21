package ch.ksobwalden.wikidump;

import ch.ksobwalden.csv.CsvWriter;
import ch.ksobwalden.xml.XmlElementParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

public class WikiDumpParser {
    public static void process(CsvWriter pageWriter, CsvWriter linkWriter, InputStream inputFile) throws XMLStreamException, IOException {
        var urlTitles = new HashSet<String>();

        var xmlParser = new XmlElementParser<>(
                PageObjectParser::new,
                "page",
                (page) -> {
                    if (page.validate(urlTitles)) {
                        urlTitles.add(page.getUrlTitle());
                        pageWriter.writeLine(page.getUrlTitle(), page.getTitle(), page.getId(), page.isRedirect());
                        for (Link link : page.getLinks()) {
                            linkWriter.writeLine(page.getUrlTitle(), link.urlTitle(), link.title(), page.isRedirect(), link.index());
                        }
                    } else {
                        System.out.println("page is not valid" + page);
                    }
                }
        );

        xmlParser.parse(inputFile);
        pageWriter.flush();
        linkWriter.flush();
    }
}
