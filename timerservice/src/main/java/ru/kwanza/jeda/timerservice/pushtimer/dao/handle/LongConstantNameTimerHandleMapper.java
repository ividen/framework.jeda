package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;

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
    public Set<String> getCompatibleTimerNames() {
        return compatibleTimerNames;
    }

    public String getTimerName() {
        return timerName;
    }

    @Required
    public void setTimerName(String timerName) {
        this.timerName = timerName;
        compatibleTimerNames.add(timerName);
    }

    @Override
    public int getSQLType() {
        return Types.BIGINT;
    }
}
