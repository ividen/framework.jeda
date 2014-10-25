package ru.kwanza.jeda.timer.berkeley;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.TimerItem;
import ru.kwanza.jeda.clusterservice.old.ClusterService;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.jeda.persistentqueue.PersistenceQueueException;
import ru.kwanza.jeda.timer.ITimerPersistentController;
import ru.kwanza.jeda.timer.TimerException;
import ru.kwanza.toolbox.SerializationHelper;
import com.sleepycat.je.*;
import com.sleepycat.je.dbi.RangeConstraint;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;


public class BerkleyTimerPersistentController implements ITimerPersistentController {

    private static final class TimerComparator implements Comparator<byte[]>, Serializable {

        public int compare(byte[] o1, byte[] o2) {
            try {
                TimerKey key1 = (TimerKey) SerializationHelper.bytesToObject(o1);
                TimerKey key2 = (TimerKey) SerializationHelper.bytesToObject(o2);
                long result = key1.getMillis() - key2.getMillis();
                if (result > 0) {
                    return 1;
                } else if (result < 0) {
                    return -1;
                }
                result = key1.getId() - key2.getId();
                return result > 0 ? 1 : result < 0 ? -1 : 0;
            } catch (Exception e) {
                throw new PersistenceQueueException(e);
            }
        }
    }

    private static class TimerBounds implements RangeConstraint {

        private long fromMillis;

        private TimerBounds(long fromMillis) {
            this.fromMillis = fromMillis;
        }

        public boolean inBounds(byte[] key) {
            try {
                TimerKey tKey = (TimerKey) SerializationHelper.bytesToObject(key);
                return tKey.getMillis() >= fromMillis;
            } catch (Exception e) {
                throw new PersistenceQueueException(e);
            }
        }
    }

    private JEConnectionFactory jeFactory;
    private String databaseName;
    private DatabaseConfig databaseConfig =
            new DatabaseConfig().setAllowCreate(true).setTransactional(true).setBtreeComparator(new TimerComparator())
                    .setOverrideBtreeComparator(true);
    private CursorConfig cursorConfig = new CursorConfig();
    private AtomicLong counter;

    public BerkleyTimerPersistentController(String databaseName, JEConnectionFactory jeFactory) {
        this.jeFactory = jeFactory;
        this.databaseName = databaseName;
        long nodeId = ClusterService.getNodeId();
        long maxID = definedMaxId(nodeId);
        counter = new AtomicLong(maxID);
    }

    private long definedMaxId(long nodeId) {
        Database database = getDatabase(nodeId);
        Cursor cursor = null;
        try {
            long maxId = 0;
            cursor = openCursor(database);
            final DatabaseEntry keyEntry = new DatabaseEntry();
            final DatabaseEntry valueEntry = new DatabaseEntry();
            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                TimerKey key = (TimerKey) SerializationHelper.bytesToObject(keyEntry.getData());
                if (key.getId() > maxId) {
                    maxId = key.getId();
                }
            }
            return maxId;
        } catch (Exception e) {
            throw new TimerException(e);
        } finally {
            closeResources(cursor);
        }
    }

    public void delete(Collection<TimerItem> result) {
        Database database;
        long nodeId = ClusterService.getNodeId();
        try {
            database = getDatabase(nodeId);
            for (TimerItem event : result) {
                database.delete(null,
                        createDatabaseEntry(new TimerKey(Long.valueOf(event.getTimerHandle()), event.getMillis())));
            }
        } catch (Exception e) {
            throw new PersistenceQueueException(e);
        }
    }

    public Collection<TimerItem> load(long size, long fromMillis) {
        final ArrayList<TimerItem> resultCollection = new ArrayList<TimerItem>();
        long nodeId = ClusterService.getNodeId();
        Database database = getDatabase(nodeId);
        loadAndDelete(size, resultCollection, database, fromMillis, false);
        return resultCollection;
    }

    private void loadAndDelete(long size, ArrayList<TimerItem> resultCollection, Database database,
                               final long fromMillis, boolean del) {
        if (size > 0) {
            Cursor cursor = null;
            try {
                cursor = openCursor(database);
                cursor.setRangeConstraint(new TimerBounds(fromMillis));
                final DatabaseEntry keyEntry = new DatabaseEntry();
                final DatabaseEntry valueEntry = new DatabaseEntry();
                keyEntry.setData(SerializationHelper.objectToBytes(new TimerKey(-1, fromMillis)));
                long count = 1;
                if (OperationStatus.SUCCESS == cursor.getSearchKeyRange(keyEntry, valueEntry, LockMode.DEFAULT)) {
                    processItem(resultCollection, del, cursor, keyEntry, valueEntry);
                    while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                        if (++count > size) {
                            break;
                        }
                        processItem(resultCollection, del, cursor, keyEntry, valueEntry);
                    }
                }
            } catch (Exception e) {
                throw new TimerException(e);
            } finally {
                closeResources(cursor);
            }
        }
    }

    private void processItem(ArrayList<TimerItem> resultCollection, boolean del, Cursor cursor, DatabaseEntry keyEntry,
                             DatabaseEntry valueEntry) throws Exception {
        TimerKey key = (TimerKey) SerializationHelper.bytesToObject(keyEntry.getData());
        Object value = SerializationHelper.bytesToObject(valueEntry.getData());
        resultCollection.add(new TimerItem((IEvent) value, key.getMillis(), String.valueOf(key.getId())));
        if (del) {
            cursor.delete();
        }
    }

    public long getSize() {
        Database database;
        long nodeId = ClusterService.getNodeId();
        try {
            database = getDatabase(nodeId);
            return database.count();
        } catch (Exception e) {
            throw new TimerException(e);
        }
    }

    public void persist(Collection<TimerItem> events) {
        Database database;
        long nodeId = ClusterService.getNodeId();
        try {
            database = getDatabase(nodeId);
            for (TimerItem item : events) {
                long id = counter.incrementAndGet();
                item.setTimerHandle(String.valueOf(id));
                final DatabaseEntry keyEntry = createDatabaseEntry(new TimerKey(id, item.getMillis()));
                final DatabaseEntry dataEntry = createDatabaseEntry(item.getEvent());
                database.put(null, keyEntry, dataEntry);
            }
        } catch (Exception e) {
            throw new TimerException(e);
        }
    }

    public Collection<TimerItem> transfer(long count, long oldNodeId) {
        try {
            final ArrayList<TimerItem> result = new ArrayList<TimerItem>();
            Database database = getTxDatabase(oldNodeId);
            loadAndDelete(count, result, database, 0, true);
            persist(result);
            return result;
        } catch (TimerException e) {
            throw e;
        } catch (Exception e) {
            throw new TimerException(e);
        }
    }

    private Database getDatabase(long nodeId) {
        return jeFactory.getConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }


    private Database getTxDatabase(long nodeId) {
        return jeFactory.getTxConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }

    protected DatabaseEntry createDatabaseEntry(Object object) throws Exception {
        if (object instanceof Long) {
            return new DatabaseEntry(SerializationHelper.longToBytes((Long) object));
        } else {
            return new DatabaseEntry(SerializationHelper.objectToBytes(object));
        }
    }

    protected Cursor openCursor(Database database) {
        return database.openCursor(null, cursorConfig);
    }

    private void closeResources(Closeable... resources) {
        for (Closeable c : resources) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
