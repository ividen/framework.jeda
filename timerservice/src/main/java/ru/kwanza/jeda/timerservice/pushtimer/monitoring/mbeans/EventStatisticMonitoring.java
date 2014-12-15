package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import ru.kwanza.jeda.timerservice.pushtimer.monitoring.EventStatistic;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Michael Yeskov
 */
public class EventStatisticMonitoring implements EventStatisticMonitoringMBean{

    @Resource
    private JMXRegistry jmxRegistry;

    private EventStatistic patient;


    public EventStatisticMonitoring(EventStatistic patient) {
        this.patient = patient;
    }

    @PostConstruct
    public void init(){
        jmxRegistry.registerInTotal("Statistics-" + patient.getName(), this);

    }

    private String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(time);
    }

    @Override
    public String getInfo1EventName() {
        return patient.getName();
    }

    @Override
    public long getConfig1Interval() {
        return patient.getInterval();
    }

    @Override
    public void setConfig1Interval(long newInterval) {
        patient.setInterval(newInterval);
    }

    @Override
    public void resetTotal() {
        patient.resetTotal();
    }

    @Override
    public String getLive1TotalStart() {
        return  formatDate(patient.getTotalStart());
    }

    @Override
    public double getLive2TotalTimeSec() {
        return patient.getTotalTimeSec();
    }

    @Override
    public long getLive3TotalCount() {
        return patient.getTotalCount();
    }

    @Override
    public double getLive4TotalThroughputPerSec() {
        return patient.getTotalThroughput();
    }

    @Override
    public String getLive5IntervalStart() {
        return formatDate(patient.getIntervalStart());
    }

    @Override
    public double getLive6IntervalTimeSec() {
        return patient.getIntervalTimeSec();
    }

    @Override
    public long getLive7IntervalCount() {
        return patient.getIntervalCount();
    }

    @Override
    public double getLive8IntervalThroughputPerSec() {
        return patient.getIntervalThroughput();
    }
}
