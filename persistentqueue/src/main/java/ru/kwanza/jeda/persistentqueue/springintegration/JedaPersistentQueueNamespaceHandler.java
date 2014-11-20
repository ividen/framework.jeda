package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Guzanov Alexander
 */
public class JedaPersistentQueueNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("queue", new PersistentQueueParser());
        registerBeanDefinitionParser("berkeley-queue", new BerkeleyQueueParser());
        registerBeanDefinitionParser("db-queue", new DBQueueParser());
        registerBeanDefinitionParser("helperBean", new HelperBeanParser());
        registerBeanDefinitionParser("event-queue", new EventQueueHelperParser());
        registerBeanDefinitionParser("named-event-queue", new NamedEventQueueHelperParser());
        registerBeanDefinitionParser("priority-event-queue", new PriorityEventQueueHelperParser());
        registerBeanDefinitionParser("named-priority-event-queue", new NamedPriorityEventQueueHelperParser());
    }
}
