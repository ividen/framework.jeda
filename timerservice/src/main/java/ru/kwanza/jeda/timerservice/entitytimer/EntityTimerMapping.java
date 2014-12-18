package ru.kwanza.jeda.timerservice.entitytimer;

import ru.kwanza.toolbox.fieldhelper.Property;

/**
 * @author Michael Yeskov
 */
public class EntityTimerMapping {
    private String timerName;
    private String propertyName;
    private Property<Object, Long> entityProperty;

    public EntityTimerMapping(String timerName, String propertyName, Property<Object, Long> entityProperty) {
        this.timerName = timerName;
        this.propertyName = propertyName;
        this.entityProperty = entityProperty;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Property<Object, Long> getEntityProperty() {
        return entityProperty;
    }

    public void setEntityProperty(Property<Object, Long> entityProperty) {
        this.entityProperty = entityProperty;
    }
}
