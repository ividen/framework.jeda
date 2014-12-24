package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Yeskov
 */
public class LongConstantNameTimerHandleMapper implements ITimerHandleMapper {

    private String timerName;
    private Set<String> compatibleTimerNames = new HashSet<String>();

    @Override
    public Object toId(TimerHandle timerHandle) {
        if (!timerName.equals(timerHandle.getTimerName())) {
            throw new RuntimeException(NOT_CONFIGURED_ERROR + timerHandle.getTimerName());
        }
        return Long.valueOf(timerHandle.getTimerId());
    }

    @Override
    public TimerHandle fromRs(ResultSet rs, int pz) throws SQLException {
        long id = rs.getLong(pz);
        return new TimerHandle(timerName, String.valueOf(id));
    }

    @Override
    public int getSQLType() {
        return Types.BIGINT;
    }

    @Override
    public Set<String> getCompatibleTimerNames() {
        return compatibleTimerNames;
    }

    @Override
    public void registerCompatibleTimer(String timerName) {
        if (this.timerName != null) {
            throw new RuntimeException("This handle mapper can work only with single timer name");
        }
        this.timerName = timerName;
        compatibleTimerNames.add(timerName);
    }
}
