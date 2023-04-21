package ch.ksobwalden.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

@FunctionalInterface
public interface XmlConsumer {
    void consume(XMLEventReader reader, StartElement startElement);
}
