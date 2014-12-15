package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface FiredTimersMemoryStorageMonitoringMBean {

    public long getInfo1MaxLimit();
    public long getInfo2SingleConsumerModeLimit();
    public long getInfo3AgainMultiConsumerModeBorder();


    public boolean getLive1CurrentSingleConsumerMode();
    public long getLive2ReservedInserts();

    public long getLive3TotalQueueSize();
    public List<String> getLive4Queues();
    public List<String> getLive5PendingForProcessing();
}
