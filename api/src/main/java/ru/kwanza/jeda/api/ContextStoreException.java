package ru.kwanza.jeda.api;

import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class ContextStoreException extends Exception {

    private List optimisticItems;
    private List otherFailedItems;

    public ContextStoreException(List optimisticItems, List otherFailedItems) {
        this.optimisticItems = optimisticItems;
        this.otherFailedItems = otherFailedItems;
    }

    public <T> List<T> getOptimisticItems() {
        return optimisticItems;
    }

    public <T> List<T> getOtherFailedItems() {
        return otherFailedItems;
    }

}
