package ru.kwanza.jeda.timerservice.pushtimer.processor;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.InternalTimerFiredEvent;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class ProcessorTx extends TransactionSynchronizationAdapter{

    private FiredTimersStorageRepository repository;
    private TimerClass timerClass;
    private Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents;

    public ProcessorTx(FiredTimersStorageRepository repository, TimerClass timerClass, Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents) {
        this.repository = repository;
        this.timerClass = timerClass;
        this.bucketIdToEvents = bucketIdToEvents;
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            repository.getFiredTimersStorage(timerClass).forgetPendingTimers(bucketIdToEvents);
        }
        repository.forgetActiveProcessors(bucketIdToEvents.keySet());
    }
}
