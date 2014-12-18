package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

/**
 * not partition friendly for partition friendly solution need numeric mapping
 * Методы To
 * @author Michael Yeskov
 */
public class StringUniversalTimerHandleMapper implements ITimerHandleMapper {

    private static final String DELIMETER = "|-|-|";
    private static final String DELIMETER_REGEXP = "\\|-\\|-\\|";

    private Set<String> compatibleTimerNames = new HashSet<String>();

    @Override
    public Object toId(TimerHandle timerHandle) {
        if (!compatibleTimerNames.contains(timerHandle.getTimerName())) {
            throw new RuntimeException(NOT_CONFIGURED_ERROR + timerHandle.getTimerName());
        }
        return timerHandle.getTimerName() + DELIMETER + timerHandle.getTimerId();
    }

    @Override
    public TimerHandle fromRs(ResultSet rs, int pz) throws SQLException {
        String idString = rs.getString(pz);
        String[] parts = idString.split(DELIMETER_REGEXP);
        if (parts.length != 2) {
            throw new RuntimeException("Data corruption in timer id=" + idString);
        }
        if (!compatibleTimerNames.contains(parts[0])) {
            throw new RuntimeException(NOT_CONFIGURED_ERROR + parts[0]);
        }

        return new TimerHandle(parts[0], parts[1]);
    }

    @Override
    public Set<String> getCompatibleTimerNames() {
        return compatibleTimerNames;
    }

    @Override
    public void registerCompatibleTimer(String timerName) {
        if (compatibleTimerNames.contains(timerName)) {
            throw  new RuntimeException("Timer with name " + timerName + " was already registered with this handleMapper");
        }
        compatibleTimerNames.add(timerName);
    }

    @Override
    public int getSQLType() {
        return Types.VARCHAR;
    }

}
