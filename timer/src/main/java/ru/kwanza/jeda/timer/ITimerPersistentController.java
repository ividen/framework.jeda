package ru.kwanza.jeda.timer;

import ru.kwanza.jeda.api.TimerItem;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public interface ITimerPersistentController {
    void delete(Collection<TimerItem> result);

    Collection<TimerItem> load(long size, long fromMillis);

    long getSize();

    void persist(Collection<TimerItem> events);

    Collection<TimerItem> transfer(long count, long oldNodeId);
}
