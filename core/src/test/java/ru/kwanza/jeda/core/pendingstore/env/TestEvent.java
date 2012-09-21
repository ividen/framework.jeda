package ru.kwanza.jeda.core.pendingstore.env;

import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IEvent;

public class TestEvent  extends AbstractEvent {

    private Integer id;
    private String param;

    public TestEvent(Integer id, String param) {
        this.id = id;
        this.param = param;
    }

    public Integer getId() {
        return id;
    }

    public String getParam() {
        return param;
    }

    @Override
    public String toString() {
        return "TestEvent{" +
                "id=" + id +
                ", param='" + param + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestEvent testEvent = (TestEvent) o;

        if (id != null ? !id.equals(testEvent.id) : testEvent.id != null) return false;
        if (param != null ? !param.equals(testEvent.param) : testEvent.param != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (param != null ? param.hashCode() : 0);
        return result;
    }

}
