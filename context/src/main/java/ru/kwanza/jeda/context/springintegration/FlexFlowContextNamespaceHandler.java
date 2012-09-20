package ru.kwanza.jeda.context.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Dmitry Zagorovsky
 */
public class FlexFlowContextNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jdbc-blob-context-controller", new JDBCBlobContextControllerParser());
        registerBeanDefinitionParser("jdbc-blob-context-controller-with-dict", new JDBCBlobContextControllerWithDictParser());
        registerBeanDefinitionParser("jdbc-object-context-controller", new JDBCObjectContextControllerParser());
        registerBeanDefinitionParser("berkeley-blob-context-controller", new BerkeleyBlobContextControllerParser());
        registerBeanDefinitionParser("berkeley-blob-context-controller-with-dict", new BerkeleyBlobContextControllerWithDictParser());
    }

}
