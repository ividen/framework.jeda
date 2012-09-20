package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ITimer;
import ru.kwanza.jeda.api.TimerItem;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class AbstractTimer<E extends IEvent> implements ITimer<E> {

    public Collection<String> cancelEvents(Collection<String> timerHandles) {
        return null;
    }

    public void registerEvents(Collection<TimerItem<E>> timerIds) {
    }

    public long size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void waitForFreeSlots() {

    }

    boolean transfer(long nodeId) {
        return false;
    }
}
