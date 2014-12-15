package ru.kwanza.jeda.timerservice.pushtimer.dao;

import ru.kwanza.jeda.timerservice.pushtimer.TimerEntity;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface IFetchCursor {
    public void open();

    /*
     * true if source ended
     */
    public boolean fetchInto(List<TimerEntity> firedTimers);

    public long getCurrentLeftBorder();

    public boolean isOpen();

    public void setCurrentRightBorder(long rightBorder);

    public void close();
}
