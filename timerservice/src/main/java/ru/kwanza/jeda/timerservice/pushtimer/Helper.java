package ru.kwanza.jeda.timerservice.pushtimer;

import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.InternalTimerFiredEvent;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class Helper {

    public static Collection<NewTimer> toTimers(long timeoutMS, Collection<TimerHandle> timerHandles) {
        Collection<NewTimer> result  = new ArrayList<NewTimer>(timerHandles.size());
        for (TimerHandle handle : timerHandles) {
            result.add(new NewTimer(handle.getTimerName(), handle.getTimerId(), timeoutMS));
        }
        return result;
    }


    public static Collection<ScheduleTimerEvent> toScheduleEvents(long timeoutMS, Collection<String> timerIds, boolean reSchedule) {
        List<ScheduleTimerEvent> events = new ArrayList<ScheduleTimerEvent>();
        for (String timerId : timerIds) {
            events.add(new ScheduleTimerEvent(timerId, timeoutMS , reSchedule));
        }
        return events;
    }

    public static Map<Long, List<InternalTimerFiredEvent>> splitByBucketId(Collection<InternalTimerFiredEvent> firedTimers) {
        Map<Long, List<InternalTimerFiredEvent>> bucketIdToEvents = new HashMap<Long, List<InternalTimerFiredEvent>>();
        for (InternalTimerFiredEvent event : firedTimers) {
            List<InternalTimerFiredEvent>  list = bucketIdToEvents.get(event.getBucketId());
            if (list == null) {
                list = new ArrayList<InternalTimerFiredEvent>();
                bucketIdToEvents.put(event.getBucketId(), list);
            }
            list.add(event);
        }
        return bucketIdToEvents;
    }

    public static Collection<? extends TimerHandle> toTimerHandles(String timerName, Collection<String> timerIds) {
        List<TimerHandle> result = new ArrayList<TimerHandle>();
        for (String timerId : timerIds) {
            result.add(new TimerHandle(timerName, timerId));
        }
        return result;
    }
}
