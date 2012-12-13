package ru.kwanza.jeda.core.queue;

/**
 * @author Guzanov Alexander
 */
public class TestTransactionalMemoryQueueWithAtamikos extends TestTransactionalMemoryQueueWithDSTrx {
    @Override
    public String getContextPath() {
        return "application-context-atomikos.xml";
    }
}
