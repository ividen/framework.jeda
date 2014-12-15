package ru.kwanza.jeda.timerservice.pushtimer.config;

import org.springframework.beans.factory.annotation.Required;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;

import java.util.Set;

/**
 * @author Michael Yeskov
 */
public class TimerClass {

    private String timerClassName;
    private IDBTimerDAO dbTimerDAO;
    private ConsumerConfig consumerConfig;

    public Set<String> getCompatibleTimerNames(){
        return dbTimerDAO.getCompatibleTimerNames();
    }

    public String getTimerClassName() {
        return timerClassName;
    }

    public IDBTimerDAO getDbTimerDAO() {
        return dbTimerDAO;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimerClass)) return false;

        TimerClass that = (TimerClass) o;

        if (timerClassName != null ? !timerClassName.equals(that.timerClassName) : that.timerClassName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return timerClassName != null ? timerClassName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TimerClass{" +
                "timerClassName='" + timerClassName + '\'' +
                '}';
    }

    //for injection only

    @Required
    public void setTimerClassName(String timerClassName) {
        this.timerClassName = timerClassName;
    }

    @Required
    public void setDbTimerDAO(IDBTimerDAO dbTimerDAO) {
        this.dbTimerDAO = dbTimerDAO;
    }

    @Required
    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }
}
