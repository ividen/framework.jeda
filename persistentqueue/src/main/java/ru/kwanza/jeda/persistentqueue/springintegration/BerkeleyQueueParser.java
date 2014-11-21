package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;

/**
 * @author Guzanov Alexander
 */
class BerkeleyQueueParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(BerkeleyQueueFactory.class);

        definitionBuilder.addPropertyValue("dbName", element.getAttribute("dbName"));
        definitionBuilder.addPropertyValue("maxSize", element.getAttribute("maxSize"));
        String connectionFactory = element.getAttribute("connectionFactory");
        definitionBuilder.addPropertyReference("connectionFactory", connectionFactory);
        definitionBuilder.addPropertyReference("manager", "jeda.IJedaManager");
        definitionBuilder.addPropertyReference("service", element.getAttribute("clusterService"));


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }
}
