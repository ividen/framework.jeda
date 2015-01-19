package ru.kwanza.jeda.core.queue;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Guzanov Alexander
 */
@ContextConfiguration("application-context-arjuna.xml")
public class TestPriorityTransactionalMemoryQueueWithArjuna extends TestPriorityTransactionalMemoryQueueWithDSTrx {
}

