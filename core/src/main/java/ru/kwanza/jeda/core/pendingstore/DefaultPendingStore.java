package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.core.*;
import ru.kwanza.jeda.api.*;
import ru.kwanza.jeda.api.helper.SinkResolver;
import ru.kwanza.toolbox.SerializationHelper;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultPendingStore implements IPendingStore {

    private static final SuspendedEventRemoveSetter SUSPENDED_EVENT_REMOVE_SETTER = new SuspendedEventRemoveSetter();

    private DBTool dbTool;
    private IAutoKey autoKey;
    private SuspendInfoRowMapper suspendInfoRowMapper;
    private SuspenderDbInteraction suspenderDbInteraction;
    private PendingStoreSqlBuilder sqlBuilder;
    private String tableName = "pending_store";
    private String idColumnName = "id";
    private String sinkNameColumnName = "sink_name";
    private String eventDescriptionColumnName = "event_description";
    private String eventDataColumnName = "event_binary";

    public DefaultPendingStore() {
    }

    public DefaultPendingStore(DBTool dbTool, IAutoKey autoKey, SuspenderDbInteraction suspenderDbInteraction) {
        this.dbTool = dbTool;
        this.autoKey = autoKey;
        this.suspenderDbInteraction = suspenderDbInteraction;
        sqlBuilder = new PendingStoreSqlBuilder(this);
        suspendInfoRowMapper = new SuspendInfoRowMapper(this);
    }

    public <E extends IEvent> ISuspender<E> getSuspender() {
        return new Suspender<E>(autoKey, suspenderDbInteraction, sqlBuilder.getInsertSql());
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = ResumeException.class)
    public void resume(Collection<Long> suspendItemsIds) throws ResumeException {
        List<Long> failedToAddEventIds = null;
        Map<String, Map<Long, IEvent>> eventByIdBySinkName = loadSuspendedEvents(suspendItemsIds);

        for (Map.Entry<String, Map<Long, IEvent>> entry : eventByIdBySinkName.entrySet()) {
            String sinkName = entry.getKey();
            Map<Long, IEvent> eventById = entry.getValue();

            SinkResolver<IEvent> resolver = new SinkResolver<IEvent>(sinkName);
            try {
                resolver.put(eventById.values());
            } catch (SinkException e) {
                failedToAddEventIds = initIfAbsent(failedToAddEventIds);
                failedToAddEventIds.addAll(eventById.keySet());
            }
        }

        finishResume(suspendItemsIds, failedToAddEventIds);
    }

    public void tryResume(Collection<Long> suspendItemsIds) throws ResumeException {
        List<Long> failedToAddEventIds = null;

        Map<String, Map<Long, IEvent>> eventByIdBySinkName = loadSuspendedEvents(suspendItemsIds);

        for (Map.Entry<String, Map<Long, IEvent>> entry : eventByIdBySinkName.entrySet()) {
            String sinkName = entry.getKey();
            Map<Long, IEvent> eventById = entry.getValue();

            SinkResolver<IEvent> resolver = new SinkResolver<IEvent>(sinkName);
            try {
                Collection<IEvent> remainingEvents = resolver.tryPut(eventById.values());
                if (!remainingEvents.isEmpty()) {
                    failedToAddEventIds = initIfAbsent(failedToAddEventIds);
                    for (IEvent event : remainingEvents) {
                        failedToAddEventIds.add(IPendingStore.SUSPEND_ID_ATTR.get(event));
                    }
                }
            } catch (SinkException e) {
                failedToAddEventIds = initIfAbsent(failedToAddEventIds);
                failedToAddEventIds.addAll(eventById.keySet());
            }
        }

        finishResume(suspendItemsIds, failedToAddEventIds);
    }

    private void finishResume(Collection<Long> suspendItemsIds, List<Long> failedToAddEventIds) throws ResumeException {
        Collection<Long> eventsToRemoveList;
        if (failedToAddEventIds == null) {
            eventsToRemoveList = suspendItemsIds;
        } else {
            eventsToRemoveList = new LinkedList<Long>(suspendItemsIds);
            eventsToRemoveList.removeAll(failedToAddEventIds);
        }

        try {
            removeSuspendedEvents(eventsToRemoveList);
        } catch (UpdateException e) {
            throw new RuntimeException("Failed to remove resumed events data.", e);
        }

        if (failedToAddEventIds != null) {
            throw new ResumeException(failedToAddEventIds);
        }
    }

    public void remove(Collection<Long> suspendItemsIds) {
        try {
            removeSuspendedEvents(suspendItemsIds);
        } catch (UpdateException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public String getSinkNameColumnName() {
        return sinkNameColumnName;
    }

    public void setSinkNameColumnName(String sinkNameColumnName) {
        this.sinkNameColumnName = sinkNameColumnName;
    }

    public String getEventDescriptionColumnName() {
        return eventDescriptionColumnName;
    }

    public void setEventDescriptionColumnName(String eventDescriptionColumnName) {
        this.eventDescriptionColumnName = eventDescriptionColumnName;
    }

    public String getEventDataColumnName() {
        return eventDataColumnName;
    }

    public void setEventDataColumnName(String eventDataColumnName) {
        this.eventDataColumnName = eventDataColumnName;
    }

    private Map<String, Map<Long, IEvent>> loadSuspendedEvents(Collection<Long> eventIds) {
        return dbTool.selectMapOfMaps(sqlBuilder.getSelectSql(), suspendInfoRowMapper, eventIds);
    }

    private void removeSuspendedEvents(Collection<Long> eventIds) throws UpdateException {
        dbTool.update(sqlBuilder.getDeleteSql(), eventIds, SUSPENDED_EVENT_REMOVE_SETTER);
    }

    protected <T> List<T> initIfAbsent(List<T> list) {
        if (list == null) {
            return new LinkedList<T>();
        } else {
            return list;
        }
    }

    private static class SuspendInfoRowMapper implements RowMapper<KeyValue<String, KeyValue<Long, IEvent>>> {

        private DefaultPendingStore pendingStore;
        private String idColumnName = null;
        private String sinkNameColumnName = null;
        private String eventDataColumnName = null;

        private SuspendInfoRowMapper(DefaultPendingStore pendingStore) {
            this.pendingStore = pendingStore;
        }

        private String getIdColumnName() {
            if (idColumnName == null) {
                idColumnName = pendingStore.getIdColumnName();
            }
            return idColumnName;
        }

        private String getSinkNameColumnName() {
            if (sinkNameColumnName == null) {
                sinkNameColumnName = pendingStore.getSinkNameColumnName();
            }
            return sinkNameColumnName;
        }

        private String getEventDataColumnName() {
            if (eventDataColumnName == null) {
                eventDataColumnName = pendingStore.getEventDataColumnName();
            }
            return eventDataColumnName;
        }

        public KeyValue<String, KeyValue<Long, IEvent>> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong(getIdColumnName());
            String sinkName = rs.getString(getSinkNameColumnName());

            byte[] data = rs.getBytes(getEventDataColumnName());

            try {
                IEvent event = (IEvent) SerializationHelper.bytesToObject(data);
                SUSPEND_ID_ATTR.set(event, id);
                return new KeyValue<String, KeyValue<Long, IEvent>>(sinkName, new KeyValue<Long, IEvent>(id, event));
            } catch (Exception e) {
                throw new RuntimeException("Couldn't deserialize event", e);
            }
        }
    }

    private static class SuspendedEventRemoveSetter implements UpdateSetter<Long> {
        public boolean setValues(PreparedStatement pst, Long id) throws SQLException {
            FieldSetter.setLong(pst, 1, id);
            return true;
        }
    }

}
