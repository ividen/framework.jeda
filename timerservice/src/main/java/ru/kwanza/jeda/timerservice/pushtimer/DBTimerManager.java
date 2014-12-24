package ru.kwanza.jeda.timerservice.pushtimer;

import ru.kwanza.dbtool.core.UpdateException;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.api.pushtimer.ITimer;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerComponent;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;
import ru.kwanza.jeda.timerservice.pushtimer.timer.TimerBatcher;
import ru.kwanza.jeda.timerservice.pushtimer.tx.PendingTxTimersStore;
import ru.kwanza.jeda.timerservice.pushtimer.tx.Tx;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Michael Yeskov
 */
public class DBTimerManager  implements ITimerManagerInternal {
    @Resource
    private TimerClassRepository timerClassRepository;

    @Resource
    private PendingTxTimersStore pendingTxTimersStore;

    @Resource
    private PendingUpdatesTimeRepository pendingUpdatesTimeRepository;

    @Resource( name = "jeda.clusterservice.DBClusterService")
    private IClusterService clusterService;

    @Resource
    private ConsumerComponent consumerComponent;

    @Resource(name="timerservice.stats.create")
    EventStatistic createStats;

    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(long timeoutMS, Collection<TimerHandle> timerHandles) {
        scheduleTimers(Helper.toTimers(timeoutMS, timerHandles));
    }

    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(String timerName, long timeoutMS, Collection<String> timerIds) {
        processScheduleTimerEvents(timerName, Helper.toScheduleEvents(timeoutMS, timerIds, false));
    }

    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(long timeoutMS, Collection<TimerHandle> timerHandles) {
        reScheduleTimers(Helper.toTimers(timeoutMS, timerHandles));
    }

    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(String timerName, long timeoutMS, Collection<String> timerIds) {
        processScheduleTimerEvents(timerName, Helper.toScheduleEvents(timeoutMS, timerIds, true));
    }


    /*
    * прямой метод обработки
    */
    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void scheduleTimers(Collection<NewTimer> timers) {
        Set<NewTimer> timerSet = checkDuplicates(timers);
        Tx tx = pendingTxTimersStore.getCurrentTx(this);
        tx.processNewTimers(timerSet, false);
    }

    /*
     * прямой метод обработки
     */
    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void reScheduleTimers(Collection<NewTimer> timers) {
        Set<NewTimer> timerSet = checkDuplicates(timers);
        Tx tx = pendingTxTimersStore.getCurrentTx(this);
        tx.processNewTimers(timerSet, true);
    }

    /*
     * прямой метод обработки
     */
    @Override
    @Transactional(TransactionalType.REQUIRED)
    public void processScheduleTimerEvents(String timerName, Collection<ScheduleTimerEvent> scheduleTimerEvents) {
        Set<ScheduleTimerEvent> eventsSet = checkDuplicates(scheduleTimerEvents);
        Tx tx = pendingTxTimersStore.getCurrentTx(this);
        tx.processScheduleEvents(timerName, eventsSet);
    }


    private <T> Set<T> checkDuplicates(Collection<T> events) {
        if (events instanceof Set) {
            return (Set) events;
        } else {
            Set<T> result  = new HashSet<T>();
            for (T event : events) {
                if (result.contains(event)) {
                    throw  new RuntimeException("Timer " + event + " has duplicates in supplied collection");
                }
                result.add(event);
            }
            return result;
        }
    }

    @Override
    public void cancelJustScheduledTimers(Collection<? extends TimerHandle> timerHandles) {
        Tx tx = pendingTxTimersStore.getCurrentTx(this);
        tx.cancelScheduling(timerHandles);
    }

    @Override
    public ITimer getTimer(String timerName) {
        return new TimerBatcher(timerName, this);
    }



    @Transactional(TransactionalType.REQUIRED)
    @Override
    public void interruptTimers(Collection<? extends TimerHandle> timers) {
        Map<TimerClass, Set<TimerHandle>> byClass = (Map) timerClassRepository.splitByClass(timers);
        for (Map.Entry<TimerClass, Set<TimerHandle>> current : byClass.entrySet()) {
            try {
                current.getKey().getDbTimerDAO().interruptTimers(current.getValue());
            } catch (UpdateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isActive(TimerHandle timer) {
        return getIsActiveMap(Arrays.asList(timer)).get(timer);
    }

    @Override
    public Map<TimerHandle, Boolean> getIsActiveMap(Collection<? extends TimerHandle> timers) {
        Map<TimerHandle, Boolean> result = new HashMap<TimerHandle, Boolean>();
        Map<TimerClass, Set<TimerHandle>> byClass = (Map) timerClassRepository.splitByClass(timers);
        for (Map.Entry<TimerClass, Set<TimerHandle>> current : byClass.entrySet()) {
            result.putAll(current.getKey().getDbTimerDAO().getIsActiveMap(current.getValue()));
        }
        return result;
    }






    @Override
    public void beforeTrxCommit(Tx tx, Map<TimerClass, Set<NewTimer>> schedule, Map<TimerClass, Set<NewTimer>> reSchedule) {
        updateDB(tx, schedule, false);
        updateDB(tx, reSchedule, true);
    }

    private void updateDB(Tx tx, Map<TimerClass, Set<NewTimer>> timers, final boolean reSchedule) {
        try {
            for (Map.Entry<TimerClass, Set<NewTimer>> current : timers.entrySet()) {
                if (current.getValue().isEmpty()) {
                    continue;
                }

                createStats.registerEvents(current.getValue().size());

                final IDBTimerDAO dao = current.getKey().getDbTimerDAO();
                final Collection<TimerEntity> toDB = pendingUpdatesTimeRepository.registerTimers(current.getKey(), current.getValue(), tx);

                clusterService.criticalSection(consumerComponent, new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (reSchedule) {
                            dao.reScheduleTimers(new HashSet<TimerEntity>(toDB), clusterService.getCurrentNode().getId());
                        } else {
                            dao.scheduleTimers(new HashSet<TimerEntity>(toDB), clusterService.getCurrentNode().getId());
                        }
                        return null;
                    }
                });
            }

        } catch (Throwable e) {
            pendingUpdatesTimeRepository.removeTimers(tx);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void afterTrxCommit(Tx tx, boolean success) {
        pendingUpdatesTimeRepository.removeTimers(tx);
    }


    @Override
    public List<TimerEntity> getReadyForFireTimers(String timerName, Set<String> timerIds) {
        TimerClass timerClass = timerClassRepository.getClassByTimerName(timerName);
        List<TimerEntity> stateInDB =  timerClass.getDbTimerDAO().loadTimerEntities(timerName, timerIds);
        long now = System.currentTimeMillis();

        List<TimerEntity> result = new ArrayList<TimerEntity>();
        for (TimerEntity currentInDB : stateInDB) {
            if ( (currentInDB.getState() == TimerState.ACTIVE) &&
                    (currentInDB.getExpireTime() <= now)) {
                result.add(currentInDB);
            }
        }
        return result;
    }

    @Override
    @Transactional(TransactionalType.MANDATORY)
    public void markFiredWithOptLock(String timerName, List<TimerEntity> timers) {
        TimerClass timerClass = timerClassRepository.getClassByTimerName(timerName);
        try {
            timerClass.getDbTimerDAO().markFiredWithOptLock(timerName, timers);
        } catch (UpdateException e) {
            throw new RuntimeException(e);
        }
    }
}
