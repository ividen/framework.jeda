package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

/**
 * @author Michael Yeskov
 */
public interface EventStatisticMonitoringMBean {

    //config
    public String getInfo1EventName();
    public long getConfig1Interval();
    public void setConfig1Interval(long newInterval);

    //operation
    public void resetTotal();


    public String getLive1TotalStart();
    public double getLive2TotalTimeSec();
    public long getLive3TotalCount();
    public double getLive4TotalThroughputPerSec();



    public String getLive5IntervalStart();
    public double getLive6IntervalTimeSec();
    public long getLive7IntervalCount();
    public double getLive8IntervalThroughputPerSec();
}
