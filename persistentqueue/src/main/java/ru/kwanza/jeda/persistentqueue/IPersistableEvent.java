package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IEvent;

import java.io.Serializable;

/**
 * @author Alexander Guzanov
 */
public interface IPersistableEvent extends IEvent, Serializable {
    Long getPersistId();

    void setPeristId(Long key);
}
