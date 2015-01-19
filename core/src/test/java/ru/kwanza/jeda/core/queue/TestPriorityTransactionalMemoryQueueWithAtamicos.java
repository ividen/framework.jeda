package ru.kwanza.jeda.core.queue;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Guzanov Alexander
 */
@ContextConfiguration("application-context-atomikos.xml")
public class TestPriorityTransactionalMemoryQueueWithAtamicos extends TestPriorityTransactionalMemoryQueueWithDSTrx {
}
