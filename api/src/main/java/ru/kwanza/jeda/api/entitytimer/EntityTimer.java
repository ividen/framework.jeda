package ru.kwanza.jeda.api.entitytimer;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Michael Yeskov
 */

@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface EntityTimer {
    String name() default IEntityTimerManager.DEFAULT_TIMER;

}
