package ru.kwanza.jeda.persistentqueue.old;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guzanov Alexander
 */
public class TestDataStore {
    private IJedaManager manager;

    private AtomicLong idCounter = new AtomicLong(0l);

    private Map<Long, ArrayList<EventWithKey>> store = new HashMap<Long, ArrayList<EventWithKey>>();
    private ReentrantLock lock = new ReentrantLock();


    private class AddSync implements Synchronization {
        private Collection<EventWithKey> events;
        private Long nodeId;

        private AddSync(Long nodeId, Collection<EventWithKey> events) {
            this.events = events;
            this.nodeId = nodeId;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int i) {
            if (i == Status.STATUS_COMMITTED) {
                add(nodeId, events);
            }

            if (i == Status.STATUS_ROLLEDBACK) {
            }
        }
    }

    private class DeleteSync implements Synchronization {
        private Collection<EventWithKey> events;
        private Long nodeId;

        private DeleteSync(Long nodeId, Collection<EventWithKey> events) {
            this.events = events;
            this.nodeId = nodeId;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int i) {
            if (i == Status.STATUS_COMMITTED) {
                remove(nodeId, events);
            }

            if (i == Status.STATUS_ROLLEDBACK) {
            }
        }
    }

    private class TransSync implements Synchronization {
        private Collection<EventWithKey> events;
        private Long currentNodeId;
        private long newNodeId;


        public TransSync(ArrayList<EventWithKey> events, long currentNodeId, long newNodeId) {
            this.events = events;
            this.currentNodeId = currentNodeId;
            this.newNodeId = newNodeId;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int i) {
            if (i == Status.STATUS_COMMITTED) {
                add(newNodeId, events);
            }

            if (i == Status.STATUS_ROLLEDBACK) {
                add(currentNodeId, events);
            }
        }
    }


    public TestDataStore(IJedaManager manager) {
        this.manager = manager;
    }

    public void add(long nodeId, Collection<EventWithKey> events) {
        lock();
        try {
            ArrayList<EventWithKey> eventWithKeyArrayList = store.get(nodeId);
            if (eventWithKeyArrayList == null) {
                eventWithKeyArrayList = new ArrayList<EventWithKey>();
                store.put(nodeId, eventWithKeyArrayList);
            }
            for (EventWithKey e : events) {
                e.setKey(idCounter.incrementAndGet());
            }
            eventWithKeyArrayList.addAll(events);
        } finally {
            unlock();
        }
    }

    public void clear() {
        store.clear();
    }

    public void delete(long nodeId, Collection<EventWithKey> events) {
        ITransactionManagerInternal tm = (ITransactionManagerInternal) manager.getTransactionManager();
        try {
            tm.getTransaction().registerSynchronization(new DeleteSync(nodeId, events));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <E extends IEvent> Collection<E> getEvents(long nodeId) {
        return EventWithKey.<E>extract(store.get(nodeId));
    }

    public ArrayList<EventWithKey> getEventsWithKey(long nodeId) {
        return store.get(nodeId);
    }

    public void lock() {
        lock.lock();
    }

    public void persist(long nodeId, Collection<EventWithKey> events) {
        ITransactionManagerInternal tm = (ITransactionManagerInternal) manager.getTransactionManager();
        try {
            tm.getTransaction().registerSynchronization(new AddSync(nodeId, events));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<EventWithKey> transfer(long count, long currentNodeId, long newNodeId) {
        lock();
        ITransactionManagerInternal tm = (ITransactionManagerInternal) manager.getTransactionManager();
        try {
            ArrayList<EventWithKey> currentEvents = store.get(currentNodeId);
            if (currentEvents == null) {
                currentEvents = new ArrayList<EventWithKey>();
                store.put(currentNodeId, currentEvents);
            }

            ArrayList<EventWithKey> events = new ArrayList<EventWithKey>();

            Iterator<EventWithKey> iterator = currentEvents.iterator();
            while (iterator.hasNext()) {
                EventWithKey next = iterator.next();
                if (events.size() >= count) {
                    break;
                }
                iterator.remove();
                events.add(next);
            }


            tm.getTransaction().registerSynchronization(new TransSync(events, currentNodeId, newNodeId));
            return events;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlock();
        }
    }

    public void unlock() {
        lock.unlock();
    }

    private void remove(long nodeId, Collection<EventWithKey> events) {
        lock();
        try {
            ArrayList<EventWithKey> eventWithKeyArrayList = store.get(nodeId);
            if (eventWithKeyArrayList == null) {
                eventWithKeyArrayList = new ArrayList<EventWithKey>();
                store.put(nodeId, eventWithKeyArrayList);
            }
            Iterator<EventWithKey> iterator = eventWithKeyArrayList.iterator();
            while (iterator.hasNext()) {
                EventWithKey next = iterator.next();
                for (EventWithKey e : events) {
                    if (e.getKey().equals(next.getKey())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        } finally {
            unlock();
        }
    }
}
