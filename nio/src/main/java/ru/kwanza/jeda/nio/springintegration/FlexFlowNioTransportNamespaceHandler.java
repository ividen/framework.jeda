package ru.kwanza.jeda.nio.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Guzanov Alexander
 */
public class FlexFlowNioTransportNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        registerBeanDefinitionParser("client-transport-flow-bus", new ClientTransportParser());
        registerBeanDefinitionParser("http-server", new HttpServerParser());
        registerBeanDefinitionParser("entry-point", new EntryPointParser());
        registerBeanDefinitionParser("jks-keystore", new JKSEntryPointKeystoreParser());
        registerBeanDefinitionParser("keystore", new CustomKeystoreParser());
        registerBeanDefinitionParser("http-handler", new HttpHandlerParser());
    }
}
