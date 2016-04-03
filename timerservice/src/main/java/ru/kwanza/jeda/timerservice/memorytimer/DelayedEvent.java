package ru.kwanza.jeda.timerservice.memorytimer;

import ru.kwanza.jeda.api.IEvent;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Yeskov
 */
public class DelayedEvent implements Delayed {


    private final IEvent event;
    private final String destinationStageName;
    private final long fireTime;

    public DelayedEvent(IEvent event, String destinationStageName, long fireTime) {

        this.event = event;
        this.destinationStageName = destinationStageName;
        this.fireTime = fireTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long inMillis = fireTime - System.currentTimeMillis();
        switch (unit) {
            case NANOSECONDS:
                return TimeUnit.MILLISECONDS.toNanos(inMillis);
            case MILLISECONDS:
                return inMillis;
            default:
                throw new IllegalArgumentException("Unsupported timeUnit");
        }
    }

    @Override
    public int compareTo(Delayed o) {
        long otherFireTime = ((DelayedEvent)o).getFireTime();
        if (fireTime < otherFireTime) {
            return -1;
        } else if (fireTime > otherFireTime) {
            return 1;
        } else {
            return 0;
        }
    }


    public IEvent getEvent() {
        return event;
    }

    public String getDestinationStageName() {
        return destinationStageName;
    }

    public long getFireTime() {
        return fireTime;
    }
}
