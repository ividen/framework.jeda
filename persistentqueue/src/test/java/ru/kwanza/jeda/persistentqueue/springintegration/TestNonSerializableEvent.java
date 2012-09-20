package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.IEvent;
import ru.kwanza.toolbox.attribute.AttributeHolder;

/**
 * @author Guzanov Alexander
 */
public class TestNonSerializableEvent implements IEvent {
    private AttributeHolder attributeHolder = new AttributeHolder();

    public String getContextId() {
        return null;
    }

    public AttributeHolder getAttributes() {
        return attributeHolder;
    }
}
