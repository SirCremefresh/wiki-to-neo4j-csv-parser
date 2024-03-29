package ch.ksobwalden;

import ch.ksobwalden.csv.CsvWriter;
import ch.ksobwalden.io.InputFile;
import ch.ksobwalden.io.OutputFileStore;
import ch.ksobwalden.wikidump.WikiDumpParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;


public class Main {

    public static void main(String[] args) throws XMLStreamException, IOException {
        // Select what wiki file you want to process
        InputFile defaultInputFile = InputFile.SIMPLE_WIKI_BZ2_REMOTE;
        InputFile inputFile = Arrays.stream(args)
                .findFirst()
                .map(InputFile::parse)
                .orElse(defaultInputFile);
        System.out.println("Parsing input file: " + inputFile);

        // The en wiki dump is pretty large, so we have to update the size limit to not trigger some security warning.
        System.setProperty("jdk.xml.totalEntitySizeLimit", String.valueOf(Integer.MAX_VALUE));
        long start = System.nanoTime();

        var inputFileReader = inputFile.load();
        var outputFiles = OutputFileStore.getOutputFilesForInput(inputFile);
        var pageWriter = new CsvWriter(outputFiles.pagesFile());
        var linkWriter = new CsvWriter(outputFiles.linksFile());

        WikiDumpParser.process(pageWriter, linkWriter, inputFileReader);

        long stop = System.nanoTime();
        System.out.println("Time: " + (stop - start) / 1000000.0 + " ms");
    }
}
