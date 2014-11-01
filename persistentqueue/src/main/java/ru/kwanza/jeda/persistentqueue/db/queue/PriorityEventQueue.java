package ru.kwanza.jeda.persistentqueue.db.queue;

import ru.kwanza.dbtool.orm.annotations.Entity;
import ru.kwanza.dbtool.orm.api.If;
import ru.kwanza.jeda.persistentqueue.DefaultPriorityPersistableEvent;
import ru.kwanza.jeda.persistentqueue.db.IEventRecordHelper;
import ru.kwanza.jeda.persistentqueue.db.base.BasePriorityEventQueue;
import ru.kwanza.toolbox.SerializationHelper;

/**
 * @author Alexander Guzanov
 */
@Entity(name = "jeda.persistentqueue.db.PriorityEventQueue", table = "jeda_jdbc_event_pqueue")
public class PriorityEventQueue<E extends DefaultPriorityPersistableEvent> extends BasePriorityEventQueue<E> {
    public PriorityEventQueue(Long id, Integer nodeId, byte[] eventData, Integer priority) {
        super(id, nodeId, eventData, priority);
    }

    public static class Helper implements IEventRecordHelper<PriorityEventQueue, DefaultPriorityPersistableEvent> {
        public Class<PriorityEventQueue> getORMClass() {
            return PriorityEventQueue.class;
        }

        public PriorityEventQueue buildRecord(DefaultPriorityPersistableEvent event, int nodeId) {
            try {
                return new PriorityEventQueue(event.getPersistId(), nodeId,
                        SerializationHelper.objectToBytes(event), event.getPriority().getCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public If getCondition() {
            return null;
        }

        public String getConditionAsString() {
            return null;
        }
    }
}
