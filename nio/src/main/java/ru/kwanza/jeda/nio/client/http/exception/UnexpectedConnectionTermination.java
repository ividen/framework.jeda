package ru.kwanza.jeda.nio.client.http.exception;

/**
 * @author Michael Yeskov
 */
public class UnexpectedConnectionTermination extends Throwable {
    public UnexpectedConnectionTermination() {
        super("Connection was terminated unexpectedly");
    }
}
