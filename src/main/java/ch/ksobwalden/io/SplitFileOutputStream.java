package ch.ksobwalden.io;

import ch.ksobwalden.common.Globals;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public class SplitFileOutputStream extends OutputStream {
    private final String fileStart;
    private final int splitSize;
    private final String fileEnd;
    private final Path folder;
    private OutputStream outputStream;

    public SplitFileOutputStream(Path baseFile, String fileStart, String fileEnd, int splitSize) {
        this.fileEnd = fileEnd;
        this.folder = baseFile;
        this.fileStart = fileStart;
        this.splitSize = splitSize;
        this.outputStream = getNewOutputStream();
    }

    private OutputStream getNewOutputStream() {
        var newOutputLocation = folder.resolve(fileStart + iteration++ + fileEnd);
        try {
            return new BufferedOutputStream(new FileOutputStream(newOutputLocation.toFile()), Globals.DEFAULT_BUFFER_SIZE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not write to output location: " + newOutputLocation, e);
        }
    }

    private int size = 0;
    private int iteration = 0;

    @Override
    public void write(int b) throws IOException {
        size++;
        outputStream.write(b);
        if (size > splitSize && b == '\n') {
            outputStream.flush();
            outputStream.close();
            outputStream = getNewOutputStream();
            size = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputStream.close();
    }
}
