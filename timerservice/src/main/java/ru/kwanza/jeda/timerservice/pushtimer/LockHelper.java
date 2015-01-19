package ru.kwanza.jeda.timerservice.pushtimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;


/**
 * @author Michael Yeskov
 */
public class LockHelper {
    private static Logger logger = LoggerFactory.getLogger(LockHelper.class);

    /*
    * Большинство реализаций lockInterruptibly вызывают внутри метод Thread.interrupted(), который сбрасывает interrupted статус у потока.
    * Соответственно, что бы поток, обрабатывающий исключения на верхнем уровне, имел возможность проверить interrupted статус мы его заново устанавливаем
    * Фактически данный метод оборачивает checked исключение InterruptedException в unchecked RuntimeException, но с возможностью проверить isInterrupted в будущем.
    */
    public static void lockInterruptibly(Lock lock) {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            //todo aguzanov зачем?
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
