package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.springintegration.FlexFlowBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class BerkeleyQueueParser extends FlexFlowBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(BerkeleyQueueFactory.class);

        String dbName = element.getAttribute("dbName");
        definitionBuilder.addPropertyValue("dbName", dbName);
        String maxSize = element.getAttribute("maxSize");
        definitionBuilder.addPropertyValue("maxSize", maxSize);
        String connectionFactory = element.getAttribute("connectionFactory");
        definitionBuilder.addPropertyReference("connectionFactory", connectionFactory);
        definitionBuilder.addPropertyReference("manager", ISystemManager.class.getName());


        return createFlexFlowDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }
}
