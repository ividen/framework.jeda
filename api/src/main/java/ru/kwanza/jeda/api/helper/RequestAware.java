package ru.kwanza.jeda.api.helper;

/**
 * @author Michael Yeskov
 */
public interface RequestAware<T> {
    public T getRequestEvent();
}
