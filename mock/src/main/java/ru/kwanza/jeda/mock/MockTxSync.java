package ru.kwanza.jeda.mock;

/**
 * @author Guzanov Alexander
 */

public interface MockTxSync {
    public void commit();

    public void rollback();
}
