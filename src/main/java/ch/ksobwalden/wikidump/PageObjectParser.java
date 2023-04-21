package ch.ksobwalden.wikidump;

import ch.ksobwalden.xml.XmlConsumer;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageObjectParser implements XmlConsumer {
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^:\\]]+?)(?:\\|([^:\\]]+?))?]]");

    private List<String> errors = null;

    private String title = null;
    private String urlTitle = null;
    private long id = -1;
    private Set<Link> links = null;
    private String text = null;
    private boolean redirect = false;

    public void consume(XMLEventReader reader, StartElement startElement) {
        var element = startElement.getName().getLocalPart();
        switch (element) {
            case "title" -> setTitle(getElementText(reader, element));
            case "id" -> setId(getElementText(reader, element));
            case "text" -> setText(getElementText(reader, element));
            case "redirect" -> setIsRedirect();
        }
    }

    private String getElementText(XMLEventReader reader, String element) {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            addError("Could not get elementText for element: " + element + ", with exception: " + e);
            return "";
        }
    }

    public boolean validate(HashSet<String> urlTitles) {
        validateUrlUnique(urlTitles);
        validateAllSet();

        return errors == null;
    }

    private void setIsRedirect() {
        redirect = true;
    }

    private void setText(String elementText) {
        Matcher matcher = LINK_PATTERN.matcher(elementText);
        links = new HashSet<>(matcher.groupCount());
        while (matcher.find()) {
            String linkTitle = matcher.group(1);
            String linkUrlTitle = titleToUrlFormat(linkTitle);
            // If the url looks like the following "[[some Link]]"
            // We add "some Link" as the title and "Some_Link" as the Url title.
            // It is also possible to add an explicit tile like the following "[[some Link| Some Link Text]]"
            // Here e add "Some Link Text" as the title and "Some_Link" as the Url title.
            // For some reason matcher.groupCount() sometimes return 2 even if there is no matcher.group(2).
            // This is why we also check for it not being null
            String linkText = matcher.groupCount() > 1 && matcher.group(2) != null ? matcher.group(2) : linkTitle;

            if (!linkUrlTitle.equals(title)) {
                links.add(new Link(linkUrlTitle, linkText, links.size()));
            }
        }
        text = elementText;
    }

    private void setId(String elementText) {
        try {
            id = Long.parseLong(elementText);
        } catch (NumberFormatException exception) {
            addError("Could not parse id to number. id: " + elementText);
        }
    }

    private void setTitle(String title) {
        this.title = title;
        this.urlTitle = titleToUrlFormat(title);
    }

    private String titleToUrlFormat(String string) {
        return capitalize(string.replaceAll(" ", "_"));
    }

    private String capitalize(String str) {
        if (str == null || str.length() <= 1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void addError(String error) {
        if (errors == null) {
            errors = new LinkedList<>();
        }
        errors.add(error);
    }

    private void validateUrlUnique(HashSet<String> urlTitles) {
        if (urlTitles.contains(urlTitle)) {
            addError("UrlTitle is not unique. urlTitle: " + urlTitle);
        }
    }

    private void validateAllSet() {
        if (urlTitle == null || urlTitle.isBlank()) {
            addError("UrlTitle can not be null or blank. urlTitle: " + title);
        }
        if (links == null) {
            addError("Links can not be null.");
        }
        if (id == -1) {
            addError("Id has to be set");
        }
    }

    @Override
    public String toString() {
        return "PageObjectParser{" +
                "errors=" + errors +
                ", title='" + title + '\'' +
                ", urlTitle='" + urlTitle + '\'' +
                ", id=" + id +
                ", links=" + links +
                ", text='" + text + '\'' +
                ", redirect=" + redirect +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public long getId() {
        return id;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public boolean isRedirect() {
        return redirect;
    }
}
