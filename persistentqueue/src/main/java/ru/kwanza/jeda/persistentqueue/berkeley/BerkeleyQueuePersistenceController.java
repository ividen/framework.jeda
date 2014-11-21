package ru.kwanza.jeda.persistentqueue.berkeley;

import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.IPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.PersistenceQueueException;
import ru.kwanza.toolbox.SerializationHelper;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Work only under JTA transaction
 *
 * @author Kiryl Karatsetski
 */
public class BerkeleyQueuePersistenceController<E extends IPersistableEvent> implements IQueuePersistenceController<E> {
    private static final Logger log = LoggerFactory.getLogger(BerkeleyQueuePersistenceController.class);
    private DatabaseConfig databaseConfig = new DatabaseConfig().setAllowCreate(true).setTransactional(true);
    private CursorConfig cursorConfig = new CursorConfig().setReadCommitted(true);
    private String databaseName;
    private JEConnectionFactory jeFactory;


    public BerkeleyQueuePersistenceController(String databaseName, JEConnectionFactory jeFactory) {
        this.databaseName = databaseName;
        this.jeFactory = jeFactory;
    }

    public void delete(Collection<E> events, Node node) {
        Database database;
        try {
            database = getDatabase(node.getId());
            for (E e : events) {
                database.delete(null, createDatabaseEntry(e.getPersistId()));
            }
        } catch (Exception e) {
            log.error("Error while deleting events", e);
            throw new PersistenceQueueException(e);
        }
    }

    private Database getDatabase(Integer nodeId) {
        return jeFactory.getConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }

    public String getQueueName() {
        return BerkeleyQueuePersistenceController.class.getSimpleName() + databaseName;
    }

    public Collection<E> load(int count, Node node) {
        final Collection<E> resultCollection = new ArrayList<E>();
        Cursor cursor = null;
        Database database = null;
        try {
            database = getDatabase(node.getId());
            cursor = openCursor(database);
            final DatabaseEntry keyEntry = new DatabaseEntry();
            final DatabaseEntry valueEntry = new DatabaseEntry();
            int i = 0;
            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                final Long key = SerializationHelper.bytesToLong(keyEntry.getData());
                final E value = (E) SerializationHelper.bytesToObject(valueEntry.getData());
                value.setPersistId(key);
                resultCollection.add(value);
                if (i++ > count) {
                    break;
                }
            }
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

    public void persist(Collection<E> events, Node node) {
        Database database = null;
        try {
            database = getDatabase(node.getId());
            for (E e : events) {
                final DatabaseEntry keyEntry = createDatabaseEntry(e.getPersistId());
                final DatabaseEntry dataEntry = createDatabaseEntry(e);
                database.put(null, keyEntry, dataEntry);
            }
        } catch (Exception e) {
            log.error("Error while persisting events", e);
            throw new PersistenceQueueException(e);
        }
    }

    public void closePersistentStore(Node node) {
        jeFactory.closeConnection(node.getId());
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
