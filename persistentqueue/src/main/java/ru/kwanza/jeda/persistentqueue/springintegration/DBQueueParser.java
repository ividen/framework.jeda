package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class DBQueueParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(getFactoryBeanClass());

        String queueName = element.getAttribute("recordHelper");
        if (StringUtils.hasText(queueName)) {
            definitionBuilder.addPropertyReference("recordHelper", queueName);
        }

        String maxSize = element.getAttribute("maxSize");
        if (StringUtils.hasText(maxSize)) {
            definitionBuilder.addPropertyValue("maxSize", maxSize);
        }


        definitionBuilder.addPropertyReference("manager", "jeda.IJedaManager");
        definitionBuilder.addPropertyReference("em","dbtool.IEntityManager");
        definitionBuilder.addPropertyReference("clusterService","jeda.clusterservice.DBClusterService");

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }

    protected Class<DBQueueFactory> getFactoryBeanClass() {
        return DBQueueFactory.class;
    }

}
