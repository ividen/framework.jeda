package ru.kwanza.jeda.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.internal.IAdmissionController;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.timerservice.internal.ITimerInternal;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ScheduleTimerEvent;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;
import ru.kwanza.jeda.timerservice.pushtimer.Helper;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class Timer extends Stage implements ITimerInternal {

    private ITimerManager timerManager;
    private TimerSink timerSink;

    public Timer(IJedaManagerInternal manager,
                 ITimerManager timerManager,
                 String name,
                 IEventProcessor processor,
                 IQueue queue, AbstractThreadManager threadManager,
                 IAdmissionController admissionController,
                 IResourceController resourceController,
                 boolean hasTransaction) {
        super(manager, name, processor, queue, threadManager, admissionController, resourceController, hasTransaction);
        this.timerManager = timerManager;
        timerSink = new TimerSink();
    }

    @Override
    public void interruptTimers(Collection<String> timerIds) {
        timerManager.interruptTimers(Helper.toTimerHandles(name, timerIds));
    }

    @Override
    public Map<String, Boolean> getIsActiveMap(Collection<String> timerIds) {
        Map<TimerHandle, Boolean> wrappedResult =  timerManager.getIsActiveMap(Helper.toTimerHandles(name, timerIds));
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (Map.Entry<TimerHandle, Boolean> entry : wrappedResult.entrySet()) {
            result.put(entry.getKey().getTimerId(), entry.getValue());
        }
        return result;
    }

    @Override
    public boolean isActive(String timerId) {
        return timerManager.isActive(new TimerHandle(name, timerId));
    }

    @Override
    public ISink getSink() {
        return timerSink;
    }

    private class TimerSink implements ISink<ScheduleTimerEvent>{
        @Override
        public void put(Collection<ScheduleTimerEvent> events) throws SinkException {
            timerManager.processScheduleTimerEvents(name, events);
        }

        @Override
        public Collection<ScheduleTimerEvent> tryPut(Collection<ScheduleTimerEvent> events) throws SinkException {
            timerManager.processScheduleTimerEvents(name, events);
            return null;
        }
    }


}
