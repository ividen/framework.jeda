package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public interface NodeSafeStopMonitoringMBean {
    public List<String> getLive1ProcessingStoppedNodes();
    public Map<String, Long> getLive2NodeActiveProcessorsCount();
    public Map<String, Long> getLive3NodeStartupPointCount();


}
