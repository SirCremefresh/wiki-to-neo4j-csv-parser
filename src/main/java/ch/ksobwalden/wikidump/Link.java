package ch.ksobwalden.wikidump;

import java.util.Objects;

/**
 * The Identity of this Entity is its urlTitle.
 * This is reflected in the equals and hashcode implementation
 */
public record Link(String urlTitle, String title, int index) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link1 = (Link) o;
        return Objects.equals(urlTitle, link1.urlTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlTitle);
    }
}
