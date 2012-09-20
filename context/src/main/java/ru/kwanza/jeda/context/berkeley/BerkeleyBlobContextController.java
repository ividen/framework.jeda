package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.dbtool.VersionGenerator;
import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContextController;
import ru.kwanza.jeda.context.MapContextImpl;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import com.sleepycat.je.*;

import java.nio.ByteBuffer;
import java.util.*;

import static ru.kwanza.jeda.clusterservice.ClusterService.getNodeId;
import static ru.kwanza.toolbox.SerializationHelper.bytesToObject;
import static ru.kwanza.toolbox.SerializationHelper.objectToBytes;

/**
 * @author Dmitry Zagorovsky
 */
public class BerkeleyBlobContextController implements IContextController<String, MapContextImpl> {

    private static final DatabaseConfig databaseConfig = new DatabaseConfig().setAllowCreate(true).setTransactional(true);

    protected static final long INITIAL_CTX_VERSION = 1l;

    private String databaseName = "blob_context";
    private JEConnectionFactory connectionFactory;
    private VersionGenerator versionGenerator;

    protected String terminator = null;

    public MapContextImpl createEmptyValue(String contextId) {
        return new MapContextImpl(contextId, terminator, null);
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public Map<String, MapContextImpl> load(Collection<String> contextIds) {
        Map<String, MapContextImpl> loadedContexts = new HashMap<String, MapContextImpl>();

        Database database = getDatabase();
        try {
            for (String ctxId : contextIds) {
                final DatabaseEntry value = new DatabaseEntry();
                final DatabaseEntry key = createDatabaseKeyEntry(new ContextKey(ctxId, terminator));
                if (database.get(null, key, value, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
                    continue;
                }

                MapContextImpl ctx = unpackContext(ctxId, value.getData());
                loadedContexts.put(ctxId, ctx);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return loadedContexts;
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public void store(Collection<MapContextImpl> contextObjects) throws ContextStoreException {
        List<MapContextImpl> insertContextItems = new LinkedList<MapContextImpl>();
        List<MapContextImpl> updateContextItems = new LinkedList<MapContextImpl>();
        for (MapContextImpl context : contextObjects) {
            if (context.getVersion() == null) {
                context.setVersion(INITIAL_CTX_VERSION);
                insertContextItems.add(context);
            } else {
                updateContextItems.add(context);
            }
        }

        Database database = null;

        if (!insertContextItems.isEmpty() || !updateContextItems.isEmpty()) {
            database = getDatabase();
        }

        if (!insertContextItems.isEmpty()) {
            storeNewContextItems(database, insertContextItems);
        }

        if (!updateContextItems.isEmpty()) {
            updateContextItems(database, updateContextItems);
        }

    }

    private void storeNewContextItems(Database database,
                                      Collection<MapContextImpl> contextObjects) throws ContextStoreException {

        List<MapContextImpl> failedToAddCtxList = null;
        for (MapContextImpl ctx : contextObjects) {
            try {
                final DatabaseEntry keyEntry = createDatabaseKeyEntry(getCtxKey(ctx));
                final DatabaseEntry dataEntry = createDatabaseValueEntry(ctx);
                database.put(null, keyEntry, dataEntry);
            } catch (Exception e) {
                failedToAddCtxList = initIfAbsent(failedToAddCtxList);
                failedToAddCtxList.add(ctx);
            }
        }

        if (failedToAddCtxList != null) {
            throw new ContextStoreException(null, failedToAddCtxList);
        }
    }

    private void updateContextItems(Database database,
                                    Collection<MapContextImpl> contextObjects) throws ContextStoreException {

        List<MapContextImpl> otherFailedItems = null;
        List<MapContextImpl> optimisticItems = null;

        for (MapContextImpl ctx : contextObjects) {
            try {
                final DatabaseEntry value = new DatabaseEntry();
                final DatabaseEntry key = createDatabaseKeyEntry(new ContextKey(ctx.getId(), terminator));

                if (database.get(null, key, value, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
                    otherFailedItems = initIfAbsent(otherFailedItems);
                    otherFailedItems.add(ctx);
                    continue;
                }

                if (ctx.getVersion().equals(unpackContextVersion(value.getData()))) {
                    Long nextVersion = versionGenerator.generate(MapContextImpl.class.getName(), ctx.getVersion());
                    ctx.setVersion(nextVersion);
                    database.put(null, key, createDatabaseValueEntry(ctx));
                } else {
                    optimisticItems = initIfAbsent(optimisticItems);
                    optimisticItems.add(ctx);
                }
            } catch (Throwable e) {
                otherFailedItems = initIfAbsent(otherFailedItems);
                otherFailedItems.add(ctx);
            }
        }

        if (optimisticItems != null || otherFailedItems != null) {
            throw new ContextStoreException(optimisticItems, otherFailedItems);
        }
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ContextStoreException.class)
    public void remove(Collection<MapContextImpl> contexts) throws ContextStoreException {
        //Элементы, которые не удалось захватить до удаленния
        List<MapContextImpl> optimisticItems = null;
        Database database = getDatabase();

        try {
            updateContextItems(database, contexts);
        } catch (ContextStoreException e) {
            optimisticItems = initIfAbsent(optimisticItems);
            optimisticItems.addAll(e.<MapContextImpl>getOptimisticItems());
        }

        //Формируем список элементов для удаления (заданные - незахваченные)
        Collection<MapContextImpl> contextToRemoveList;
        if (optimisticItems == null) {
            contextToRemoveList = contexts;
        } else {
            contextToRemoveList = new LinkedList<MapContextImpl>(contexts);
            contextToRemoveList.removeAll(optimisticItems);
        }

        List<MapContextImpl> otherFailedItems = null;
        for (MapContextImpl ctx : contextToRemoveList) {
            try {
                database.delete(null, createDatabaseKeyEntry(ctx));//Результат не проверям
            } catch (Exception e) {
                otherFailedItems = initIfAbsent(otherFailedItems);
                otherFailedItems.add(ctx);
            }
        }

        if (optimisticItems != null || otherFailedItems != null) {
            throw new ContextStoreException(optimisticItems, otherFailedItems);
        }
    }

    protected List<MapContextImpl> initIfAbsent(List<MapContextImpl> list) {
        if (list == null) {
            return new LinkedList<MapContextImpl>();
        } else {
            return list;
        }
    }

    protected byte[] packContext(MapContextImpl ctx) throws Exception {
        byte[] mapData = packContextInnerMap(ctx.getInnerMap());
        return ByteBuffer.allocate(8 + mapData.length).
                putLong(ctx.getVersion() == null ? INITIAL_CTX_VERSION : ctx.getVersion()).
                put(mapData).array();
    }

    protected byte[] packContextInnerMap(Map<String, Object> innerMap) throws Exception {
        return objectToBytes(innerMap);
    }

    protected Long unpackContextVersion(byte[] data) {
        return ByteBuffer.wrap(data).getLong();
    }

    protected MapContextImpl unpackContext(String id, byte[] data) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(data);
        Long version = bb.getLong();

        byte[] innerMapBytes = new byte[bb.remaining()];
        bb.get(innerMapBytes);

        return new MapContextImpl(id, terminator, version, unpackContextInnerMap(innerMapBytes));
    }

    protected Map<String, Object> unpackContextInnerMap(byte[] innerMapBytes) throws Exception {
        //noinspection unchecked
        return (Map<String, Object>) bytesToObject(innerMapBytes);
    }

    private DatabaseEntry createDatabaseKeyEntry(ContextKey ctxKey) throws Exception {
        if (ctxKey.getContextId() == null) {
            throw new IllegalArgumentException("Context id can't be null.");
        }
        return new DatabaseEntry(objectToBytes(ctxKey));
    }

    private DatabaseEntry createDatabaseKeyEntry(MapContextImpl ctx) throws Exception {
        return createDatabaseKeyEntry(new ContextKey(ctx));
    }

    private DatabaseEntry createDatabaseValueEntry(MapContextImpl ctx) throws Exception {
        return new DatabaseEntry(packContext(ctx));
    }

    private Database getDatabase() {
        long nodeId = getNodeId();
        return connectionFactory.getConnection(nodeId).openDatabase(databaseName, databaseConfig);
    }

    private ContextKey getCtxKey(MapContextImpl ctx) {
        return new ContextKey(ctx.getId(), terminator);
    }

    public String getTerminator() {
        return terminator;
    }

    public void setTerminator(String terminator) {
        this.terminator = terminator;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setConnectionFactory(JEConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setVersionGenerator(VersionGenerator versionGenerator) {
        this.versionGenerator = versionGenerator;
    }

}
