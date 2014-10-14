package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

/**
 * @author Guzanov Alexander
 */
public abstract class TestPriorityPersistentQueue extends TestPersistentQueue {

    @Override
    protected PersistentQueue createQeueue() {
        return new PriorityPersistentQueue((ISystemManagerInternal)
                ctx.getBean("ru.kwanza.jeda.api.ISystemManager"), 1000, controller);
    }

    @Override
    protected Event createEvent(String contextId) {
        return new PriorityEvent(contextId);
    }
}
