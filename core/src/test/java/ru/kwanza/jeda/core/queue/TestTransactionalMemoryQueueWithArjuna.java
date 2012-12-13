package ru.kwanza.jeda.core.queue;

/**
 * @author Guzanov Alexander
 */
public class TestTransactionalMemoryQueueWithArjuna extends TestTransactionalMemoryQueueWithDSTrx {
    @Override
    public String getContextPath() {
        return "application-context-arjuna.xml";
    }
}

