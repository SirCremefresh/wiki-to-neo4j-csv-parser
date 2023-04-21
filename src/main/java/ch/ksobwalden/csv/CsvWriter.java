package ch.ksobwalden.csv;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;

public class CsvWriter {
    private final OutputStream outputStream;

    public CsvWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void writeLine(Object... values) {
        try {
            writeValue(values[0]);
            for (int i = 1; i < values.length; i++) {
                outputStream.write(',');
                writeValue(values[i]);
            }
            outputStream.write('\n');
        } catch (Exception e) {
            throw new RuntimeException("Could not write csv to output stream. e: ", e);
        }

    }

    private void writeValue(Object value) throws IOException {
        if (value instanceof Number || value instanceof Boolean) {
            outputStream.write(value.toString().getBytes());
        } else {
            outputStream.write(escape(value.toString()).getBytes());
        }
    }

    private static String escape(String input) {
        return "\"" + StringUtils.replace(StringUtils.replace(input, "\n", "_"), "\"", "\"\"") + "\"";
    }
}
