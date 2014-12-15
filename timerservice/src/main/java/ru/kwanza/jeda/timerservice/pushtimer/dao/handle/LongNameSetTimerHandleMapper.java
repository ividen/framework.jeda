package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author Michael Yeskov
 */
public class LongNameSetTimerHandleMapper implements ITimerHandleMapper {

    private Set<String> compatibleTimerNames;

    private Map<String, Long> timerNameToKey = new HashMap<String, Long>();
    private Map<Long, String> timerKeyToName = new HashMap<Long, String>();

    private static final long SHIFT = 10;

    @Override
    public Object toId(TimerHandle timerHandle) {
        return  (Long.valueOf(timerHandle.getTimerId()) * SHIFT) + timerNameToKey.get(timerHandle.getTimerName());
    }

    @Override
    public TimerHandle fromRs(ResultSet rs, int pz) throws SQLException {
        long id = rs.getLong(pz);

        String timerId = String.valueOf(id / SHIFT);
        String timerName = timerKeyToName.get(id % SHIFT);
        return new TimerHandle(timerName, timerId );
    }

    public Set<String> getCompatibleTimerNames() {
        return compatibleTimerNames;
    }

    @Required
    public void setCompatibleTimerNames(Set<String> compatibleTimerNames) {
        if (compatibleTimerNames.size() > 10) {
            throw new IllegalArgumentException("This mappers supports max 10 timerNames");
        }
        long currentKey = 0;
        for (String name : compatibleTimerNames) {
            timerNameToKey.put(name , currentKey);
            timerKeyToName.put(currentKey, name);
            currentKey++;
        }
        this.compatibleTimerNames = compatibleTimerNames;
    }

    @Override
    public int getSQLType() {
        return Types.BIGINT;
    }
}
