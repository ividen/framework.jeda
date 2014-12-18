package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Michael Yeskov
 */
public class LongNameSetTimerHandleMapper implements ITimerHandleMapper {

    private Set<String> compatibleTimerNames = new TreeSet<String>();

    private Map<String, Long> timerNameToKey = new HashMap<String, Long>();
    private Map<Long, String> timerKeyToName = new HashMap<Long, String>();

    private static final long SHIFT = 10;

    private volatile boolean dirty = true;
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public Object toId(TimerHandle timerHandle) {
        lazyLoad();
        return  (Long.valueOf(timerHandle.getTimerId()) * SHIFT) + timerNameToKey.get(timerHandle.getTimerName());
    }

    @Override
    public TimerHandle fromRs(ResultSet rs, int pz) throws SQLException {
        lazyLoad();
        long id = rs.getLong(pz);

        String timerId = String.valueOf(id / SHIFT);
        String timerName = timerKeyToName.get(id % SHIFT);
        return new TimerHandle(timerName, timerId );
    }

    public Set<String> getCompatibleTimerNames() {
        return compatibleTimerNames;
    }

    @Override
    public void registerCompatibleTimer(String timerName) {
        if (compatibleTimerNames.size() > 9) {
            throw new IllegalArgumentException("This mappers supports max 10 timerNames");
        }
        compatibleTimerNames.add(timerName);
    }

    @Override
    public int getSQLType() {
        return Types.BIGINT;
    }

    private void lazyLoad() {
        if (dirty) {
            lock.lock();
            try {
                if (dirty) {
                    long currentKey = 0;
                    for (String timerName : compatibleTimerNames) {
                        timerNameToKey.put(timerName , currentKey);
                        timerKeyToName.put(currentKey, timerName);
                        currentKey++;
                    }
                    dirty = false;
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
