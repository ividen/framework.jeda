package ru.kwanza.jeda.core.tm;

import java.io.IOException;

/**
 * @author Guzanov Alexander
 */
public class TestIOException extends IOException {
    public TestIOException() {
    }

    public TestIOException(String message) {
        super(message);
    }
}
