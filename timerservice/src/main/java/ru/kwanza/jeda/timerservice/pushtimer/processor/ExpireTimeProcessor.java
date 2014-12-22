package ru.kwanza.jeda.timerservice.pushtimer.processor;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.timerservice.pushtimer.processor.TimerFiredEvent;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.memory.FiredTimersStorageRepository;
import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.InternalTimerFiredEvent;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Michael Yeskov
 */
public class ExpireTimeProcessor implements IEventProcessor<InternalTimerFiredEvent> {

    private IEventProcessor<TimerFiredEvent> delegate;

    @Resource
    private TimerClassRepository timerClassRepository;

    @Resource (name = "jeda.IJedaManager")
    private IJedaManager jedaManager;

    @Resource (name = "jeda.ITimerManager")
    private ITimerManagerInternal timerManager;

    @Resource
    private FiredTimersStorageRepository firedTimersStorageRepository;

    @Resource (name = "timerservice.stats.process")
    private EventStatistic processStats;

    @Override
    public void process(Collection<InternalTimerFiredEvent> events) {
        String timerName = jedaManager.getCurrentStage().getName();
        TimerClass timerClass = timerClassRepository.getClassByTimerName(timerName);

        Set<String> filteredTimerIds  = filterEvents(events, timerClass);

        List<TimerFiredEvent> readyEventsForDelegate = new ArrayList<TimerFiredEvent>();


        List<TimerEntity> dbState = timerManager.getReadyForFireTimers(timerName, filteredTimerIds);

        Collections.sort(dbState); //to avoid deadlocks

        for (TimerEntity currentState : dbState) {
            readyEventsForDelegate.add(new TimerFiredEvent(timerName, currentState.getTimerId()));
        }

        delegate.process(readyEventsForDelegate);

        processStats.registerEvents(readyEventsForDelegate.size());

        timerManager.markFiredWithOptLock(timerName, dbState);
    }

    /*
     * filter by banned buckets
     * obsolete startup point count
     * remove duplicates by id
     *
     *
     * Register synchronization for update pending storage and active consumers for bucket on commit     *
     */

    private Set<String> filterEvents(Collection<InternalTimerFiredEvent> events, TimerClass timerClass) {
        Set<String> filteredTimerIds = new HashSet<String>();
        Map<Long, Long> registeredBucketsToPointCount = null;

        try {
            Set<Long> bucketIds = new HashSet<Long>();
            for (InternalTimerFiredEvent event : events){
                bucketIds.add(event.getBucketId());
            }

            registeredBucketsToPointCount = firedTimersStorageRepository.registerActiveProcessor(bucketIds);


            Map<Long, List<InternalTimerFiredEvent>> bucketIdToEventsFiltered = new HashMap<Long, List<InternalTimerFiredEvent>>();

            for (InternalTimerFiredEvent event : events) {
                Long  pointCount = registeredBucketsToPointCount.get(event.getBucketId());
                if (pointCount != null) {
                    List<InternalTimerFiredEvent> list = bucketIdToEventsFiltered.get(event.getBucketId());
                    if (list == null) {
                        list = new ArrayList<InternalTimerFiredEvent>();
                        bucketIdToEventsFiltered.put(event.getBucketId(), list);
                    }
                    if (pointCount.equals(event.getNodeStartupPointCount())) {
                        list.add(event);
                        filteredTimerIds.add(event.getTimerId());
                    }
                }

            }

            try {
                jedaManager.getTransactionManager().getTransaction().registerSynchronization(
                        new ProcessorTx(firedTimersStorageRepository, timerClass, bucketIdToEventsFiltered));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            if (registeredBucketsToPointCount != null) {
                firedTimersStorageRepository.forgetActiveProcessors(registeredBucketsToPointCount.keySet());
            }
            throw  e;
        }

        return filteredTimerIds;
    }


    public IEventProcessor<TimerFiredEvent> getDelegate() {
        return delegate;
    }

    @Required
    public void setDelegate(IEventProcessor<TimerFiredEvent> delegate) {
        this.delegate = delegate;
    }

}
