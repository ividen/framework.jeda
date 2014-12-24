package ru.kwanza.jeda.timerservice.pushtimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;


/**
 * @author Michael Yeskov
 */
public class LockHelper {
    private static Logger logger = LoggerFactory.getLogger(LockHelper.class);
    public static void lockInterruptibly(Lock lock) {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
