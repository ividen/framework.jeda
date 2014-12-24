package ru.kwanza.jeda.timerservice.pushtimer.timer;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.ISink;
import ru.kwanza.jeda.api.internal.IAdmissionController;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.ITimerInternal;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.core.stage.Stage;
import ru.kwanza.jeda.core.threadmanager.AbstractThreadManager;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class Timer extends Stage implements ITimerInternal {

    private TimerBatcher timerBatcher;

    public Timer(IJedaManagerInternal manager,
                 ITimerManager timerManager,
                 String name,
                 IEventProcessor processor,
                 IQueue queue, AbstractThreadManager threadManager,
                 IAdmissionController admissionController,
                 IResourceController resourceController,
                 boolean hasTransaction) {
        super(manager, name, processor, queue, threadManager, admissionController, resourceController, hasTransaction);
        this.timerBatcher = new TimerBatcher(name, timerManager);
    }

    @Override
    public void interruptTimers(Collection<String> timerIds) {
        timerBatcher.interruptTimers(timerIds);
    }

    @Override
    public Map<String, Boolean> getIsActiveMap(Collection<String> timerIds) {
        return timerBatcher.getIsActiveMap(timerIds);
    }

    @Override
    public boolean isActive(String timerId) {
        return timerBatcher.isActive(timerId);
    }

    @Override
    public void cancelJustScheduledTimers(Collection<String> timerIds) {
        timerBatcher.cancelJustScheduledTimers(timerIds);
    }

    @Override
    public ISink getSink() {
        return timerBatcher.getSink();
    }
}
