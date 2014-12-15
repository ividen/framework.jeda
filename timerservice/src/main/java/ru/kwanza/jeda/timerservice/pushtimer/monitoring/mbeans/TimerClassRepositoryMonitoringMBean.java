package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface TimerClassRepositoryMonitoringMBean {
    public List<String> getTimerClassToTimerNames();
}
