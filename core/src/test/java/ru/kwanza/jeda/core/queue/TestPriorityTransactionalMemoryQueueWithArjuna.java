package ru.kwanza.jeda.core.queue;

/**
 * @author Guzanov Alexander
 */
public class TestPriorityTransactionalMemoryQueueWithArjuna extends TestPriorityTransactionalMemoryQueueWithDSTrx {

    @Override
    public String getContextPath() {
        return "application-context-arjuna.xml";
    }
}

