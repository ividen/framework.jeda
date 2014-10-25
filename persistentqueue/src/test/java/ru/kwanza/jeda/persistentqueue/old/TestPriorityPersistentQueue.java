package ru.kwanza.jeda.persistentqueue.old;

import ru.kwanza.jeda.api.IJedaManager;

/**
 * @author Guzanov Alexander
 */
public abstract class TestPriorityPersistentQueue extends TestPersistentQueue {

    @Override
    protected PersistentQueue createQeueue() {
        return new PriorityPersistentQueue((IJedaManager)
                ctx.getBean(IJedaManager.class), 1000, controller);
    }

    @Override
    protected Event createEvent(String contextId) {
        return new PriorityEvent(contextId);
    }
}
