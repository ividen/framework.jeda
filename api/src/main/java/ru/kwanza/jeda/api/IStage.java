package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public interface IStage {

    public String getName();

    public <E extends IEvent> ISink<E> getSink();
}
