package ru.kwanza.jeda.timerservice.pushtimer.manual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.ITimerManager;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.NewTimer;
import ru.kwanza.jeda.timerservice.pushtimer.StatisticsCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Yeskov
 */
public class Inserter implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Inserter.class);
    private static final long ID_SHIFT = 1000;
    private static final String timerName = "DEFAULT_TIMER";

    private IJedaManager jedaManager;
    private ITimerManager timerManager;
    private long runnerId;
    private long currentId = 0;


    public Inserter(IJedaManager jedaManager, ITimerManager timerManager, long runnerId) {
        this.jedaManager = jedaManager;
        this.timerManager = timerManager;
        this.runnerId = runnerId;
    }

    @Override
    public void run() {
        for (int i=0; i<100000000; i++) {
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
        List<NewTimer> timers = new ArrayList<NewTimer>(1000);
        ITransactionManagerInternal tm = jedaManager.getTransactionManager();

        for (int step = 1; step <= 10; step++) {
            timers.clear();
            for (int i=0; i<1000; i++) {
                currentId++;
                timers.add(new NewTimer(timerName, String.valueOf(currentId * ID_SHIFT + runnerId), 30000 + step * i));
            }
            try {
                tm.begin();
                timerManager.scheduleTimers(timers);
                tm.commit();
                StatisticsCalculator.insert.register(timers.size());
            } catch (RuntimeException e){
                try {
                    if (tm.hasTransaction()) {
                        tm.rollback();
                    }
                } catch (Exception e1) {
                    //ignore
                }
            }
        }
    }
}
