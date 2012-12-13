package ru.kwanza.jeda.core.queue;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithAtamicos extends TestPriorityTransactionalMemoryQueueWithDSTrx {

    @Override
    public String getContextPath() {
        return "application-context-atomikos.xml";
    }
}
