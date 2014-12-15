package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface ScheduleTimerCommitMonitoringMBean {
    public long getLive1ActiveTrxCount();
    public List<String> getLive2OldestPendingUpdate();


}
