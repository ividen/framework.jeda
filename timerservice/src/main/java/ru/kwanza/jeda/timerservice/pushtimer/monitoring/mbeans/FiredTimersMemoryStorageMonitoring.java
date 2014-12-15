package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersMemoryStorage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;

/**
 * @author Michael Yeskov
 */
public class FiredTimersMemoryStorageMonitoring extends AbstractCachedMonitoring implements FiredTimersMemoryStorageMonitoringMBean {
    private FiredTimersMemoryStorage patient;

    //local cache
    private long totalQueueSize = 0;
    private List<String> queues = new ArrayList<String>();
    private List<String> pendingForProcessing = new ArrayList<String>();


    public FiredTimersMemoryStorageMonitoring(FiredTimersMemoryStorage patient) {
        this.patient = patient;
    }

    @Override
    public long getInfo1MaxLimit() {
        return patient.getMaxLimit();
    }

    @Override
    public long getInfo2SingleConsumerModeLimit() {
        return patient.getSingleConsumerModeLimit();
    }

    @Override
    public long getInfo3AgainMultiConsumerModeBorder() {
        return patient.getAgainMultiConsumerModeBorder();
    }

    @Override
    public boolean getLive1CurrentSingleConsumerMode() {
        return patient.getCurrentSingleConsumerMode();
    }

    @Override
    public long getLive2ReservedInserts() {
        return patient.getReservedInserts();
    }

    @Override
    public long getLive3TotalQueueSize() {
        return getFromCache(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return totalQueueSize;
            }
        });
    }

    @Override
    public List<String> getLive4Queues() {
        return getFromCache(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return queues;
            }
        });
    }

    @Override
    public List<String> getLive5PendingForProcessing() {
        return getFromCache(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return pendingForProcessing;
            }
        });
    }

    @Override
    protected void fillCache() {
        queues.clear();
        pendingForProcessing.clear();
        totalQueueSize = 0;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        patient.lock();
        try {
            for (Map.Entry<String, IQueue> entry : patient.getTimerNameToQueue().entrySet()) {
                long size =  entry.getValue().size();
                queues.add(entry.getKey() + " (size = " + size + ")");
                totalQueueSize += size;
            }

            for (Map.Entry<Long, SortedMap<Long, Long>> entry : patient.getBucketIdToPendingSortedByExpiry().entrySet()) {
                String minExpiry = "none";
                if (!entry.getValue().isEmpty()) {
                    minExpiry = format.format(entry.getValue().firstKey());
                }
                pendingForProcessing.add("Node" + entry.getKey() + " (minExpiry = " + minExpiry + ")");
            }
        } finally {
            patient.unlock();
        }
    }
}
