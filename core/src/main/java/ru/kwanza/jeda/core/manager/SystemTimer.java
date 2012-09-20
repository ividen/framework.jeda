package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.ITimer;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class SystemTimer<E extends IEvent> implements ITimer<E> {
    private ITimer timer;
    private String name;

    public SystemTimer(String name, ITimer timer) {
        this.timer = timer;
        this.name = name;
    }


    public Collection<String> cancelEvents(Collection<String> timerHandles) {
        return timer.cancelEvents(timerHandles);
    }

    public void registerEvents(Collection timerIds) {
        timer.registerEvents(timerIds);
    }

    public long size() {
        return timer.size();
    }

    public String getName() {
        return name;
    }
}
