package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Michael Yeskov
 */
public class JedaTimersNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("timer", new TimerParser());

        registerBeanDefinitionParser("timer-class", new TimerClassParser());
        registerBeanDefinitionParser("timer-class-ref", new RefBeanParser());

        registerBeanDefinitionParser("consumer-config-ref", new RefBeanParser());
        registerBeanDefinitionParser("consumer-config", new ConsumerConfigParser());


        registerBeanDefinitionParser("dao-custom", new RefBeanParser());
        registerBeanDefinitionParser("dao-insert-delete", DAOParser());
        registerBeanDefinitionParser("dao-insert-single-update", DAOParser());
        registerBeanDefinitionParser("dao-insert-multi-update", DAOParser());
        registerBeanDefinitionParser("dao-updating", DAOParser());

        registerBeanDefinitionParser("mapping", new MappingParser());

    }
}
