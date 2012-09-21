package ru.kwanza.jeda.core.pendingstore;

import ru.kwanza.dbtool.DBTool;
import ru.kwanza.dbtool.FieldSetter;
import ru.kwanza.dbtool.UpdateException;
import ru.kwanza.dbtool.UpdateSetter;
import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_ID_ATTR;
import static ru.kwanza.jeda.api.IPendingStore.SUSPEND_SINK_NAME_ATTR;
import static ru.kwanza.toolbox.SerializationHelper.objectToBytes;

public class SuspenderDbInteraction {

    private static final EventInsertSetter EVENT_INSERT_SETTER = new EventInsertSetter();

    private DBTool dbTool;

    @SuppressWarnings("UnusedDeclaration")
    public SuspenderDbInteraction() {
    }

    public SuspenderDbInteraction(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    @Transactional(value = TransactionalType.REQUIRED, applicationExceptions = UpdateException.class)
    public void store(String insertSql, List<IEvent> suspends) throws UpdateException {
        dbTool.update(insertSql, suspends, EVENT_INSERT_SETTER);
    }

    private static class EventInsertSetter implements UpdateSetter<IEvent> {
        public boolean setValues(PreparedStatement pst, IEvent event) throws SQLException {
            FieldSetter.setLong(pst, 1, SUSPEND_ID_ATTR.get(event));
            FieldSetter.setString(pst, 2, SUSPEND_SINK_NAME_ATTR.get(event));
            FieldSetter.setString(pst, 3, event.toString());
            try {
                FieldSetter.setBlob(pst, 4, objectToBytes(event));
            } catch (Exception e) {
                throw new RuntimeException("Couldn't serialize event", e);
            }
            return true;
        }
    }

}
