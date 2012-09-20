package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ITimer;
import ru.kwanza.jeda.api.TimerItem;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestTimer<E extends IEvent> implements ITimer<E> {
    public Collection<String> cancelEvents(Collection<String> timerHandles) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerEvents(Collection<TimerItem<E>> timerIds) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public long size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
