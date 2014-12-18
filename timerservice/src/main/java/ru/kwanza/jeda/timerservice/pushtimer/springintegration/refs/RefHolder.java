package ru.kwanza.jeda.timerservice.pushtimer.springintegration.refs;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Michael Yeskov
 */
public abstract class RefHolder {
    private String ref;

    public String getRef() {
        return ref;
    }

    @Required
    public void setRef(String ref) {
        this.ref = ref;
    }
}
