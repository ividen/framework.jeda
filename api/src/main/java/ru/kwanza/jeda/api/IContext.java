package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public interface IContext<ID, VER> {

    ID getId();

    VER getVersion();

}
