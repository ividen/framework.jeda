package ru.kwanza.jeda.persistentqueue.old;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IQueuePersistenceController {
    void delete(Collection<EventWithKey> result, long nodeId);

    Collection<EventWithKey> load(long nodeId);

    void persist(Collection<EventWithKey> events, long nodeId);

    Collection<EventWithKey> transfer(int count, long currentNodeId, long newNodeId);
}
