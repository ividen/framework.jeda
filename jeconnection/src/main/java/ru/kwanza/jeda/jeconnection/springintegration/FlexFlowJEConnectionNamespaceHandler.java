package ru.kwanza.jeda.jeconnection.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Guzanov Alexander
 */
public class FlexFlowJEConnectionNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("factory", new JEConnectionFactoryParser());
    }
}
