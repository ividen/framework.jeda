package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.jeda.api.IPriorityEvent;

import java.io.Serializable;

/**
 * @author Alexander Guzanov
 */
public interface IPriorityPersistableEvent extends IPriorityEvent, Serializable {
    Long getPersistId();
}
