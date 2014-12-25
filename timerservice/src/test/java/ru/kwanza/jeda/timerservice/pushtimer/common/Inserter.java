package ru.kwanza.jeda.timerservice.pushtimer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.api.helper.SinkHelper;
import ru.kwanza.jeda.api.pushtimer.ScheduleTimerEvent;
import ru.kwanza.jeda.api.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.timerservice.pushtimer.StatisticsCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Yeskov
 */
public class Inserter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Inserter.class);
    private static final long ID_SHIFT = 1000;
    private String timerName = "DEFAULT_TIMER";

    private IJedaManager jedaManager;
    private ITimerManager timerManager;
    private TransactionTemplate newTx;
    private long runnerId;
    private long currentId = 0;


    private boolean useHelper = true;
    private boolean useSink = false;


    public Inserter(IJedaManager jedaManager, ITimerManager timerManager, long runnerId) {
        this.jedaManager = jedaManager;
        this.timerManager = timerManager;
        this.runnerId = runnerId;
        this.newTx = new TransactionTemplate(jedaManager.getTransactionManager(),
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
    }

    public Inserter(String timerName, IJedaManager jedaManager, ITimerManager timerManager, long runnerId, boolean useSink) {
        this(jedaManager, timerManager, runnerId);
        this.timerName = timerName;
        this.useSink = useSink;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100000000; i++) {
            long start = System.currentTimeMillis();
            doWork();
            long stop = System.currentTimeMillis();
            long sleep = 1000 - (stop - start);
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    private void doWork() {
        final List<NewTimer> timers = new ArrayList<NewTimer>(1000);
        final List<ScheduleTimerEvent> timerEvents = new ArrayList<ScheduleTimerEvent>(1000);
        final SinkHelper sinkHelper = new SinkHelper(jedaManager);

        for (int step = 1; step <= 3; step++) {
            timers.clear();
            for (int i = 0; i < 1000; i++) {
                currentId++;
                NewTimer current = new NewTimer(timerName, String.valueOf(currentId * ID_SHIFT + runnerId), 30000 + step * i);
                if (useSink) {
                    ScheduleTimerEvent currentEvent = new ScheduleTimerEvent(current.getTimerId(), current.getTimeoutMS());
                    if (useHelper) {
                        sinkHelper.put(timerName, currentEvent);
                    } else {
                        timerEvents.add(currentEvent);
                    }
                } else {
                    timers.add(current);
                }

            }
            try {

                newTx.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        if (useSink) {
                            try {
                                if (useHelper) {
                                    sinkHelper.flush();
                                } else {
                                    jedaManager.getTimer(timerName).<ScheduleTimerEvent>getSink().put(timerEvents);
                                }
                            } catch (SinkException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            timerManager.scheduleTimers(timers);
                        }
                    }
                });
                StatisticsCalculator.insert.register(timers.size());
            } catch (RuntimeException e) {
                logger.error("Error in inserter!", e);
            }
        }
    }
}
