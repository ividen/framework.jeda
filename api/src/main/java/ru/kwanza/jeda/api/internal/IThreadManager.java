package ru.kwanza.jeda.api.internal;

/**
 * @author Guzanov Alexander
 */
public interface IThreadManager {

    public void adjustThreadCount(IStageInternal stage, int count);

    public int getThreadCount();

}
