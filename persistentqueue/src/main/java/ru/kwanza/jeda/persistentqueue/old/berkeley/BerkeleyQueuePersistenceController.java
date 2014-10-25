package ru.kwanza.jeda.persistentqueue.old.berkeley;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.toolbox.SerializationHelper;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;
import ru.kwanza.jeda.persistentqueue.old.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.old.PersistenceQueueException;
import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Work only under JTA transaction
 *
 * @author Kiryl Karatsetski
 */
public class BerkeleyQueuePersistenceController implements IQueuePersistenceController {
    private static final Logger log = LoggerFactory.getLogger(BerkeleyQueuePersistenceController.class);

    private AtomicLong counter = new AtomicLong(0l);
    private DatabaseConfig databaseConfig = new DatabaseConfig().setAllowCreate(true).setTransactional(true);
    private CursorConfig cursorConfig = new CursorConfig().setReadCommitted(true);
    private String databaseName;
    private JEConnectionFactory jeFactory;


    public BerkeleyQueuePersistenceController(String databaseName, JEConnectionFactory jeFactory) {
        this.databaseName = databaseName;
        this.jeFactory = jeFactory;
    }

    public void delete(Collection<EventWithKey> events, long nodeId) {
        Database database = null;
        try {
            database = getDatabase(nodeId);
            for (EventWithKey eventWithKey : events) {
                database.delete(null, createDatabaseEntry(eventWithKey.getKey()));
            }
        } catch (Exception e) {
            log.error("Error while deleting events", e);
            throw new PersistenceQueueException(e);
        }
    }

    private Database getDatabase(long nodeId) {
        return jeFactory.getConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }

    public Collection<EventWithKey> load(long nodeId) {
        final Collection<EventWithKey> resultCollection = new ArrayList<EventWithKey>();
        Cursor cursor = null;
        Database database = null;
        try {
            database = getDatabase(nodeId);
            cursor = openCursor(database);
            final DatabaseEntry keyEntry = new DatabaseEntry();
            final DatabaseEntry valueEntry = new DatabaseEntry();
            long maxKey = 0;
            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                final Long key = SerializationHelper.bytesToLong(keyEntry.getData());
                final Object value = SerializationHelper.bytesToObject(valueEntry.getData());
                if (maxKey < key) {
                    maxKey = key;
                }
                resultCollection.add(new EventWithKey(key, (IEvent) value));
            }
            counter.compareAndSet(0, maxKey);
        } catch (Exception e) {
            log.error("Error while loading events", e);
            throw new PersistenceQueueException(e);
        } finally {
            closeResources(cursor);
        }
        return resultCollection;
    }

    private void closeResources(Closeable... resources) {
        for (Closeable c : resources) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    log.error("Error closing resource {}", c);
                    log.error("Error closing resource ", e);
                }
            }
        }
    }

    public void persist(Collection<EventWithKey> events, long nodeId) {
        Database database = null;
        try {
            database = getDatabase(nodeId);
            for (EventWithKey eventWithKey : events) {
                eventWithKey.setKey(counter.incrementAndGet());
                final DatabaseEntry keyEntry = createDatabaseEntry(eventWithKey.getKey());
                final DatabaseEntry dataEntry = createDatabaseEntry(eventWithKey.getDelegate());
                database.put(null, keyEntry, dataEntry);
            }
        } catch (Exception e) {
            log.error("Error while persisting events", e);
            throw new PersistenceQueueException(e);
        }
    }

    public Collection<EventWithKey> transfer(int count, long currentNodeId, long newNodeId) {
        final Collection<EventWithKey> resultCollection = new ArrayList<EventWithKey>();
        Database refusedDatabase = null;
        Database newDatabase = null;
        Cursor cursor = null;
        try {
            refusedDatabase = jeFactory.getTxConnection(currentNodeId).openDatabase(databaseName, databaseConfig);
            newDatabase = getDatabase(newNodeId);
            cursor = openCursor(refusedDatabase);

            final DatabaseEntry keyEntry = new DatabaseEntry();
            final DatabaseEntry valueEntry = new DatabaseEntry();

            long index = count;

            while ((cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS)
                    && (index--) > 0) {
                final Long key = SerializationHelper.bytesToLong(keyEntry.getData());
                final Object value = SerializationHelper.bytesToObject(valueEntry.getData());
                resultCollection.add(new EventWithKey(key, (IEvent) value));

                cursor.delete();

                keyEntry.setData(SerializationHelper.longToBytes(counter.incrementAndGet()));
                newDatabase.put(null, keyEntry, valueEntry);
            }
        } catch (Exception e) {
            log.error("Error while transfering events", e);
            throw new PersistenceQueueException(e);
        } finally {
            closeResources(cursor);
        }

        return resultCollection;
    }

    protected Cursor openCursor(Database database) {
        return database.openCursor(null, cursorConfig);
    }

    protected DatabaseEntry createDatabaseEntry(Object object) throws Exception {
        if (object instanceof Long) {
            return new DatabaseEntry(SerializationHelper.longToBytes((Long) object));
        } else {
            return new DatabaseEntry(SerializationHelper.objectToBytes(object));
        }
    }
}
