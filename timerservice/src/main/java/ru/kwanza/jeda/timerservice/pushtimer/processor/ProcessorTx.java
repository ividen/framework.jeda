package ru.kwanza.jeda.timerservice.pushtimer.processor;

import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.InternalTimerFiredEvent;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class ProcessorTx implements Synchronization{

    private FiredTimersStorageRepository repository;
    private TimerClass timerClass;
    private Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents;

    public ProcessorTx(FiredTimersStorageRepository repository, TimerClass timerClass, Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents) {
        this.repository = repository;
        this.timerClass = timerClass;
        this.bucketIdToEvents = bucketIdToEvents;
    }

    @Override
    public void beforeCompletion() {

    }

    @Override
    public void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
            repository.getFiredTimersStorage(timerClass).forgetPendingTimers(bucketIdToEvents);
        }
        repository.forgetActiveProcessors(bucketIdToEvents.keySet());
    }
}
