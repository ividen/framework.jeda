package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.clusterservice.old.ClusterService;
import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.toolbox.SerializationHelper;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static ru.kwanza.toolbox.SerializationHelper.bytesToLong;

public class BerkeleyDictionaryDbInteractor implements DictionaryDbInteractor {

    private static final Logger log = LoggerFactory.getLogger(BerkeleyDictionaryDbInteractor.class);
    private static final DatabaseConfig databaseConfig = new DatabaseConfig().setAllowCreate(true).setTransactional(true);
    private static final CursorConfig cursorConfig = new CursorConfig().setReadCommitted(true);

    private AtomicLong counter = new AtomicLong(0l);

    private JEConnectionFactory jeFactory;

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Long storeNewProperty(String propertyName, ContextDictionaryController dictCtrl) {
        Database database = getDatabase(dictCtrl.getDictionaryTableName());

        DatabaseEntry key = createDatabaseKeyEntry(propertyName);
        Long id = counter.incrementAndGet();
        DatabaseEntry value = createDatabaseValueEntry(id);

        if (database.putNoOverwrite(null, key, value) == OperationStatus.KEYEXIST) {
            id = readIdFromDb(propertyName, dictCtrl);
        }
        return id;
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Long readIdFromDb(String propertyName, ContextDictionaryController dictCtrl) {
        Database database = getDatabase(dictCtrl.getDictionaryTableName());

        DatabaseEntry key = createDatabaseKeyEntry(propertyName);
        DatabaseEntry value = new DatabaseEntry();

        if (database.get(null, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return bytesToLong(value.getData());
        } else {
            return null;
        }
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Map<String, Long> readAllDictionary(ContextDictionaryController dictCtrl) {
        Map<String, Long> idByName = new HashMap<String, Long>();
        Database database = getDatabase(dictCtrl.getDictionaryTableName());
        Cursor cursor = null;
        try {
            cursor = openCursor(database);
            final DatabaseEntry keyEntry = new DatabaseEntry();
            final DatabaseEntry valueEntry = new DatabaseEntry();

            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                idByName.put(new String(keyEntry.getData()), bytesToLong(valueEntry.getData()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(cursor);
        }
        return idByName;
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public String readNameFromDb(Long propertyId, ContextDictionaryController dictCtrl) {
        //Метод не должен быть вызван вообще -- все данные должны быть в кеше
        log.error("Something going wrong with ContextDictionaryController.dictionaryCache");
        Database database = getDatabase(dictCtrl.getDictionaryTableName());

        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();
        Cursor cursor = null;
        try {
            cursor = openCursor(database);
            while (cursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if (propertyId == bytesToLong(valueEntry.getData())) {
                    return new String(keyEntry.getData());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(cursor);
        }
        throw new RuntimeException("Property with id '" + propertyId + "' not found");
    }

    private Database getDatabase(String databaseName) {
        long nodeId = ClusterService.getNodeId();
        return jeFactory.getConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }

    private Cursor openCursor(Database database) {
        return database.openCursor(null, cursorConfig);
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

    protected DatabaseEntry createDatabaseKeyEntry(String str) {
        return new DatabaseEntry(str.getBytes());
    }

    protected DatabaseEntry createDatabaseValueEntry(Long id) {
        return new DatabaseEntry(SerializationHelper.longToBytes(id));
    }

    public void setJeFactory(JEConnectionFactory jeFactory) {
        this.jeFactory = jeFactory;
    }

}
