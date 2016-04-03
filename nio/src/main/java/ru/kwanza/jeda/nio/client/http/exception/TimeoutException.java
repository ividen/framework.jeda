package ru.kwanza.jeda.nio.client.http.exception;

/**
 * @author Michael Yeskov
 */
public class TimeoutException extends Throwable{

    public TimeoutException(Throwable cause) {
        super(cause);
    }
}
