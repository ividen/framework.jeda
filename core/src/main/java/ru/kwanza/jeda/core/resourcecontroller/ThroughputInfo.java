package ru.kwanza.jeda.core.resourcecontroller;

import java.util.Comparator;

/**
 * @author: Guzanov Alexander
 */
class ThroughputInfo extends ThroughputRateHelper {
    private int batchSize;
    private ThroughputInfo prev;
    private ThroughputInfo next;
    private boolean hasCalculation;
    private int currentIterationCount = 0;

    static final Comparator COMPARATOR = new Comparator<ThroughputInfo>() {
        public int compare(ThroughputInfo o1, ThroughputInfo o2) {
            if (o2.getRate() > o1.getRate()) {
                return 1;
            } else if (o2.getRate() < o1.getRate()) {
                return -1;
            } else {
                return o2.getBatchSize() - o1.getBatchSize();
            }
        }
    };

    ThroughputInfo(int batchSize, RateAdjustInfo adjustInfo,
                   double throughputRate, double realThroughputRate, ThroughputInfo prev, ThroughputInfo next) {
        super(adjustInfo, throughputRate, realThroughputRate);
        this.batchSize = batchSize;
        this.prev = prev;
        this.next = next;
    }

    public ThroughputInfo getPrev() {
        return prev;
    }

    public ThroughputInfo getNext() {
        return next;
    }

    public void setPrev(ThroughputInfo prev) {
        this.prev = prev;
    }

    public void setNext(ThroughputInfo next) {
        this.next = next;
    }

    public boolean isFirst() {
        return prev == null;
    }

    public boolean isLast() {
        return next == null;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
