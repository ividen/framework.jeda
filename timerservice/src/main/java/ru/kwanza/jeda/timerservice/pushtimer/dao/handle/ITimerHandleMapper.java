package ru.kwanza.jeda.timerservice.pushtimer.dao.handle;

import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Michael Yeskov
 */
public interface ITimerHandleMapper {
    public static final  String NOT_CONFIGURED_ERROR = "This handle is not configured to work with timerName = ";

    public Object toId(TimerHandle timerHandle);
    public TimerHandle fromRs(ResultSet rs, int pz) throws SQLException;
    public Set<String> getCompatibleTimerNames ();

    /*
     * field java.sql.Types  for update
     */
    public int getSQLType();
}
