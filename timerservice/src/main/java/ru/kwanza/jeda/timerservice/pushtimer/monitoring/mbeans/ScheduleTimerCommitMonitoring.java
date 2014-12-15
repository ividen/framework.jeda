package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import org.springframework.stereotype.Component;
import ru.kwanza.jeda.timerservice.pushtimer.PendingUpdatesTimeRepository;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Michael Yeskov
 */
@Component
public class ScheduleTimerCommitMonitoring extends AbstractCachedMonitoring implements ScheduleTimerCommitMonitoringMBean {

    private long activeTrxCount = 0;
    private List<String> oldestPendingUpdate = new ArrayList<String>();


    @Resource
    private PendingUpdatesTimeRepository patient;
    @Resource
    private JMXRegistry jmxRegistry;

    @PostConstruct
    public void init() {
        jmxRegistry.registerInTotal(ScheduleTimerCommitMonitoring.class.getSimpleName(), this);
    }

    @Override
    public long getLive1ActiveTrxCount() {
        return getFromCache(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return activeTrxCount;
            }
        });
    }

    @Override
    public List<String> getLive2OldestPendingUpdate() {
        return getFromCache(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return oldestPendingUpdate;
            }
        });
    }

    @Override
    protected void fillCache() {
        oldestPendingUpdate.clear();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        patient.lock();
        try {
            activeTrxCount = patient.getMinPendingExpiry().size();
            for (Map.Entry<TimerClass, SortedMap<Long, AtomicLong>> entry : patient.getExpiryToTimerCount().entrySet()) {

                String currentOldestPending = "none";
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    currentOldestPending = format.format(entry.getValue().firstKey());
                }
                oldestPendingUpdate.add(entry.getKey().getTimerClassName() + " = " + currentOldestPending);

            }
        } finally {
            patient.unlock();
        }
    }

}
