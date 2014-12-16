package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Michael Yeskov
 */
public class JedaTimersNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("timer", new TimerParser());
        registerBeanDefinitionParser("timer-class-ref", new TimerClassParser());



    }
}
