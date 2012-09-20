package ru.kwanza.jeda.core.tm;

import javax.transaction.Transaction;

/**
 * @author Guzanov Alexander
 */
final class SuspendMarker {
    Transaction tx;

    SuspendMarker(Transaction tx) {
        this.tx = tx;
    }
}
