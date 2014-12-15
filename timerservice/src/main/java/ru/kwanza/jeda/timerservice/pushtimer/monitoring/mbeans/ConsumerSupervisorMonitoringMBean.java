package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import java.util.List;

/**
 * @author Michael Yeskov
 */
public interface ConsumerSupervisorMonitoringMBean {

    public String getInfo1NodeId();
    public String getInfo2TimerClassName();
    public String getInfo3ConsumerConfigStr();
    public long getInfo4SleepTimeout();
    public List<String> getInfo5AllConsumers();

    public String getLive01FailoverLeftBorder();
    public String getLive02ConsumerLeftBorder();
    public String getLive03ConsumerRightBorder();
    public String getLive04AvailableRightBorder();


    public List<String> getLive05IdleConsumers();
    public List<String> getLive06WorkingConsumers();
    public List<String> getLive07SuspendedByQuotaConsumers();

    public boolean getLive08AliveValue();
    public boolean isLive09ThreadAlive();
    public boolean isLive10ThreadInterrupted();

}
