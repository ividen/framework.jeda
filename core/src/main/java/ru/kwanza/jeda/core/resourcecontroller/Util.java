package ru.kwanza.jeda.core.resourcecontroller;

/**
 * @author Guzanov Alexander
 */
final class Util {
    static final double IMPRECISION = 0.1d;
    static final double INCREASE_BATCH_MULTIPLIER = 0.1d;
    static final double KICK_DOWN_BATCH_MULTIPLIER = 0.5d;
    static final double MEASUREMENT_TIME = 1000;
    static final double SMOOTH_ALPHA = 0.7;
    static final int DEFAULT_MAX_BATCH_SIZE = 10000;
    static final int DEFAULT_MAX_THREAD_COUNT = 4;
    static final int DEFAULT_START_BATCH_SIZE = 1000;
    static final long DEFAULT_MAX_BATCH_PROCESSING_THRESHOLD = 60 * 1000;


    public static double smooth_average(double newValue, double oldValue, double alfa) {
        return newValue * alfa + (1 - alfa) * oldValue;
    }

    public static int compare(double newValue, double prevValue, double imprecision) {
        if (newValue > prevValue * (1 + imprecision)) {
            return 1;
        }

        if (newValue < prevValue * (1 - imprecision)) {
            return -1;
        }

        return 0;
    }
}
