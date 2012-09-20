package ru.kwanza.jeda.api;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface ITimer<E extends IEvent> {
    Collection<String> cancelEvents(Collection<String> timerHandles);

    void registerEvents(Collection<TimerItem<E>> timerIds);

    long size();
}
