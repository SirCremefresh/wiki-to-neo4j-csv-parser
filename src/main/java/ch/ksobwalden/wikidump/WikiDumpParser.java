package ch.ksobwalden.wikidump;

import ch.ksobwalden.csv.CsvWriter;
import ch.ksobwalden.xml.XmlElementParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

public class WikiDumpParser {
    private final HashSet<String> urlTitles = new HashSet<>();
    private final CsvWriter pageWriter;
    private final CsvWriter linkWriter;
    private final XmlElementParser<PageObjectParser> xmlParser;

    public WikiDumpParser(CsvWriter pageWriter, CsvWriter linkWriter) {
        this.pageWriter = pageWriter;
        this.linkWriter = linkWriter;
        this.xmlParser = new XmlElementParser<>(
                PageObjectParser::new,
                "page",
                this::onPageCompleted
        );
    }

    public void process(InputStream inputFile) throws XMLStreamException, IOException {
        xmlParser.parse(inputFile);
        pageWriter.flush();
        linkWriter.flush();
    }

    private void onPageCompleted(PageObjectParser page) {
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
}
