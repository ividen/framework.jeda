package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerThread;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor.ConsumerSupervisorThread;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Michael Yeskov
 */
public class ConsumerSupervisorMonitoring extends AbstractCachedMonitoring implements ConsumerSupervisorMonitoringMBean {
    private ConsumerSupervisorThread patient;

    //full copy for consistency
    private volatile List<String> idleConsumers = new ArrayList<String>();
    private volatile List<String> workingConsumers = new ArrayList<String>();
    private volatile List<String> suspendedByQuotaConsumers = new ArrayList<String>();
    private String failoverLeftBorder = "";
    private String consumerLeftBorder = "";
    private String consumerRightBorder = "";
    private String availableRightBorder = "";

    public ConsumerSupervisorMonitoring(ConsumerSupervisorThread patient) {
        this.patient = patient;
    }

    @Override
    public String getInfo1NodeId() {
        return patient.getNodeId().toString();
    }

    @Override
    public String getInfo2TimerClassName() {
        return patient.getTimerClass().getTimerClassName();
    }

    @Override
    public String getInfo3ConsumerConfigStr() {
        return patient.getConfig().toString();
    }

    @Override
    public long getInfo4SleepTimeout() {
        return patient.getSleepTimeout();
    }

    @Override
    public List<String> getInfo5AllConsumers() {
        List<String> result = new ArrayList<String>(patient.getAllConsumers().size());
        for (ConsumerThread thread : patient.getAllConsumers()) {
            result.add(thread.getName());
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public String getLive01FailoverLeftBorder() {
        return getFromCache(new Callable<String>() {
            public String call() throws Exception {
                return failoverLeftBorder;
            }
        });
    }

    @Override
    public String getLive02ConsumerLeftBorder() {
        return getFromCache(new Callable<String>() {
            public String call() throws Exception {
                return consumerLeftBorder;
            }
        });

    }

    @Override
    public String getLive03ConsumerRightBorder() {
        return getFromCache(new Callable<String>() {
            public String call() throws Exception {
                return consumerRightBorder;
            }
        });
    }

    @Override
    public String getLive04AvailableRightBorder() {
        return getFromCache(new Callable<String>() {
            public String call() throws Exception {
                return availableRightBorder;
            }
        });
    }

    @Override
    public List<String> getLive05IdleConsumers() {
        return getFromCache(new Callable<List<String>>() {
            public List<String> call() throws Exception {
                return idleConsumers;
            }
        });
    }

    @Override
    public List<String> getLive06WorkingConsumers() {
        return getFromCache(new Callable<List<String>>() {
            public List<String> call() throws Exception {
                return workingConsumers;
            }
        });
    }

    @Override
    public List<String> getLive07SuspendedByQuotaConsumers() {
        return getFromCache(new Callable<List<String>>() {
            public List<String> call() throws Exception {
                return suspendedByQuotaConsumers;
            }
        });
    }

    @Override
    public boolean getLive08AliveValue() {
        return patient.getAliveValue();
    }

    @Override
    public boolean isLive09ThreadAlive() {
        return patient.isThreadAlive();
    }

    @Override
    public boolean isLive10ThreadInterrupted() {
        return patient.isThreadInterrupted();
    }

    protected void fillCache() {
        LinkedList<ConsumerThread> idleConsumersCopy;
        SortedMap<Long, ConsumerThread> workingConsumersCopy;
        SortedMap<Long, ConsumerThread> suspendedByQuotaConsumersCopy;
        long failoverLeftBorderCopy;
        long consumerLeftBorderCopy;
        long consumerRightBorderCopy;
        long availableRightBorderCopy;

        patient.lockProcessing();
        try {
            idleConsumersCopy = new LinkedList<ConsumerThread>(patient.getIdleConsumers());
            workingConsumersCopy = new TreeMap<Long, ConsumerThread>(patient.getWorkingConsumers());
            suspendedByQuotaConsumersCopy = new TreeMap<Long, ConsumerThread>(patient.getSuspendedByQuotaConsumers());
            failoverLeftBorderCopy = patient.getFailoverLeftBorder();
            consumerLeftBorderCopy = patient.getConsumerLeftBorder();
            consumerRightBorderCopy = patient.getConsumerRightBorder();
            availableRightBorderCopy = patient.getAvailableRightBorder();
        } finally {
            patient.unlockProcessing(); //release processing as soon as possible
        }

        idleConsumers = getStringDescription(idleConsumersCopy);
        Collections.sort(idleConsumers);
        workingConsumers = getStringDescription(workingConsumersCopy.values());
        suspendedByQuotaConsumers = getStringDescription(suspendedByQuotaConsumersCopy.values());
        failoverLeftBorder = formatDate(failoverLeftBorderCopy);
        consumerLeftBorder = formatDate(consumerLeftBorderCopy);
        consumerRightBorder = formatDate(consumerRightBorderCopy);
        availableRightBorder = formatDate(availableRightBorderCopy);
    }

    private List<String> getStringDescription(Collection<ConsumerThread> threads) {
        List<String> result = new ArrayList<String>(threads.size());
        for (ConsumerThread thread : threads) {
            result.add(thread.getName() + "(" + formatDate(thread.getInitialLeftBorder()) + " -> " + formatDate(thread.getLeftBorder()) + " , " + formatDate(thread.getRightBorder()) + ")");
        }
        return result;
    }

    private String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(time);
    }



}
