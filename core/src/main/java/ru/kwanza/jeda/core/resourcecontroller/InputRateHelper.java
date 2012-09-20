package ru.kwanza.jeda.core.resourcecontroller;

import java.util.concurrent.locks.ReentrantLock;

import static ru.kwanza.jeda.core.resourcecontroller.Util.SMOOTH_ALPHA;
import static ru.kwanza.jeda.core.resourcecontroller.Util.smooth_average;

/**
 * @author Guzanov Alexander
 */
public class InputRateHelper extends ReentrantLock {
    private volatile double rate = 0;
    private long count;
    private long interval = 0;
    private RateAdjustInfo info;

    public InputRateHelper(RateAdjustInfo info) {
        this.info = info;
    }

    public double getRate() {
        return rate;
    }

    public final boolean calculate(long count) {
        lock();
        try {
            long ts = System.currentTimeMillis();
            this.count += count;
            if (interval == 0) {
                interval = ts;
                this.rate = count;
                return true;
            } else {
                long delta = ts - interval;
                if (delta >= info.getInterval()) {
                    double inputRate = (double) this.count * 1000 / (double) (delta);
                    this.rate = smooth_average(inputRate, this.rate, SMOOTH_ALPHA);
                    interval = ts;
                    this.count = 0;
                    return true;
                }
            }
        } finally {
            unlock();
        }

        return false;
    }

}