package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IPriorityEvent;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
class PriorityEventComparator implements Comparator<IPriorityEvent> {
    public static final PriorityEventComparator INSTANCE = new PriorityEventComparator();


    public int compare(IPriorityEvent o1, IPriorityEvent o2) {
        return o2.getPriority().getCode() - o1.getPriority().getCode();
    }
}
