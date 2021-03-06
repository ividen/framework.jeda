package ru.kwanza.jeda.persistentqueue.db.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.db.base.BaseEventQueue;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.db.EventQueue", table = "jeda_event_queue")
public class EventQueue<E extends DefaultPersistableEvent> extends BaseEventQueue<E> {

    public EventQueue(Long id, Integer nodeId, byte[] eventData) {
        super(id, nodeId, eventData);
    }

    public static class Helper extends BaseEventQueueHelper<EventQueue, DefaultPersistableEvent> {
        public Class<EventQueue> getORMClass() {
            return EventQueue.class;
        }

        public EventQueue buildRecord(DefaultPersistableEvent event, int nodeId) {
            try {
                return new EventQueue(event.getPersistId(), nodeId, SerializationHelper.objectToBytes(event));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
