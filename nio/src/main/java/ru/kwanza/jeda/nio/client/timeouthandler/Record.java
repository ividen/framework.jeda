package ru.kwanza.jeda.nio.client.timeouthandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexander Guzanov
 */
final class Record {
    AtomicLong timestamp;
    AtomicBoolean throwReadError;

    Record(long timeout) {
        this.timestamp = new AtomicLong(System.currentTimeMillis() + timeout);
        throwReadError = new AtomicBoolean(false);
    }

}
