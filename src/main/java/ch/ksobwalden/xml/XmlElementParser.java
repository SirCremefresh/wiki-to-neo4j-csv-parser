package ch.ksobwalden.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class XmlElementParser<E extends XmlConsumer> {
    private final Supplier<E> consumerCreator;
    private final String elementTag;
    private final Consumer<E> tagFinished;

    public XmlElementParser(Supplier<E> consumerCreator, String elementTag, Consumer<E> tagFinished) {
        this.consumerCreator = consumerCreator;
        this.elementTag = elementTag;
        this.tagFinished = tagFinished;
    }

    public void parse(InputStream fileStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(fileStream);

        E parser = null;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equals(elementTag)) {
                    parser = consumerCreator.get();
                } else if (parser != null) {
                    parser.consume(reader, startElement);
                }
            }
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(elementTag) && parser != null) {
                    tagFinished.accept(parser);
                    parser = null;
                }
            }
        }
    }
}
