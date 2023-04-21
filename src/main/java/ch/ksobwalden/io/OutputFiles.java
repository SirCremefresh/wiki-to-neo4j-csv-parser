package ch.ksobwalden.io;

import java.io.OutputStream;

public record OutputFiles(OutputStream pagesFile, OutputStream linksFile) {
}
