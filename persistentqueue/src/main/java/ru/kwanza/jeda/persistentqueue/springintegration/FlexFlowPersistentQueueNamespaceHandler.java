package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Guzanov Alexander
 */
public class FlexFlowPersistentQueueNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("queue", new PersistentQueueParser());
        registerBeanDefinitionParser("berkeley-queue", new BerkeleyQueueParser());
        registerBeanDefinitionParser("jdbc-queue", new JDBCQueueParser());
    }
}
