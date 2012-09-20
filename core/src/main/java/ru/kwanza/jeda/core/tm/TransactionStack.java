package ru.kwanza.jeda.core.tm;

import javax.transaction.Transaction;
import java.util.Stack;

/**
 * @author Guzanov Alexander
 */
class TransactionStack extends Stack {
    Transaction currentTransaction;

    public Transaction getNext() {
        if (isEmpty()) {
            return null;
        }
        if (peek() instanceof SuspendMarker) {
            return null;
        }
        return (Transaction) pop();
    }
}
