package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

/**
 * @author Guzanov Alexander
 */
public abstract class TestPriorityPersistentQueue extends TestPersistentQueue {

    @Override
    protected PersistentQueue createQeueue() {
        return new PriorityPersistentQueue((IJedaManagerInternal)
                ctx.getBean("ru.kwanza.jeda.api.IJedaManager"), 1000, controller);
    }

    @Override
    protected Event createEvent(String contextId) {
        return new PriorityEvent(contextId);
    }
}
