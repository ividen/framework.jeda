package ru.kwanza.jeda.core.resourcecontroller;

import java.util.concurrent.locks.ReentrantLock;

import static ru.kwanza.jeda.core.resourcecontroller.Util.SMOOTH_ALPHA;
import static ru.kwanza.jeda.core.resourcecontroller.Util.smooth_average;

/**
 * @author Guzanov Alexander
 */
public class ThroughputRateHelper extends ReentrantLock {
    private volatile double rate = 0;
    private volatile double realRate = 0;
    private long count;
    private long realCount;
    private long interval = 0;
    private RateAdjustInfo adjustInfo;
    private volatile boolean ready = false;

    public ThroughputRateHelper(RateAdjustInfo adjustInfo) {
        this.adjustInfo = adjustInfo;
    }

    public ThroughputRateHelper(RateAdjustInfo adjustInfo, double rate, double realRate) {
        this.adjustInfo = adjustInfo;
        this.rate = rate;
        this.realRate = realRate;
    }

    public RateAdjustInfo getAdjustInfo() {
        return adjustInfo;
    }

    public double getRate() {
        return rate;
    }

    public double getRealRate() {
        return realRate;
    }

    public boolean calculate(int count, int batchSize, long millis) {
        lock();
        try {
            this.realCount += count;
            this.count += batchSize;
            if (millis == 0) {
                millis = 1;
            }
            interval += millis;


            if (
                    interval >= adjustInfo.getInterval()) {
                double rate = (double) this.count * 1000 / (double) (interval);
                this.rate = smooth_average(rate, this.rate, SMOOTH_ALPHA);

                rate = (double) realCount * 1000 / (double) (interval);
                this.realRate = smooth_average(rate, this.realRate, SMOOTH_ALPHA);
                this.realCount = 0;

                this.interval = 0;
                this.count = 0;
                ready = true;
                return true;
            }

        } finally {
            unlock();
        }

        return false;
    }

//    public boolean calculate(int count, int batchSize, long millis) {
//        lock();
//        try {
//            this.realCount += count;
//            this.count += batchSize;
//            realInterval += millis;
////            interval += millis;
//            long ts = System.currentTimeMillis();
//            if(interval==0){
//                interval = ts;
//            }
//            currentIterationCount++;
//            long delta = ts - interval;
//
//            if (/*(currentIterationCount > adjustInfo.getIterationCount() ||
//                    this.count >= adjustInfo.getCount()) && */
//                    delta >= adjustInfo.getInterval()) {
//                currentIterationCount = 0;
//                if (delta == 0) {
//                    delta = 1;
//                }
//                double rate = (double) this.count * 1000 / (double) (delta);
//                this.rate = smooth_average(rate, this.rate, SMOOTH_ALPHA);
//                this.count = 0;
//
//                currentIterationCount = 0;
//                rate = (double) realCount * 1000 / (double) (delta);
//                this.realRate = smooth_average(rate, this.realRate, SMOOTH_ALPHA);
//                this.realCount = 0;
//
//                interval = ts;
//                ready = true;
//                return true;
//            }
//
//        } finally {
//            unlock();
//        }
//
//        return false;
//    }

    public boolean isReady() {
        return ready;
    }

}
