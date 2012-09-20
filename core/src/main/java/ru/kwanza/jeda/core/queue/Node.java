package ru.kwanza.jeda.core.queue;

import ru.kwanza.jeda.api.IEvent;

/**
 * @author: Guzanov Alexander
 */
class Node<E extends IEvent> {
    protected E event;
    protected Node next;

    Node(E event) {
        this.event = event;
    }

    Node(E event, Node next) {
        this.event = event;
        this.next = next;
    }
}
