package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface IQueuePersistenceController {

    void delete(Collection<EventWithKey> result);

    Collection<EventWithKey> load(int count);

    void persist(Collection<EventWithKey> events);

    int transfer(int count, Node currentNode, Node repairableNode);

    String getQueueName();
}
