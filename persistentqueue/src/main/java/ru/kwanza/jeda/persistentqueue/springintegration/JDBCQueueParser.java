package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class JDBCQueueParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(JDBCQueueFactory.class);

        String queueName = element.getAttribute("queueName");
        if (StringUtils.hasText(queueName)) {
            definitionBuilder.addPropertyValue("queueName", queueName);
        }

        definitionBuilder.addPropertyReference("manager", ISystemManager.class.getName());

        String maxSize = element.getAttribute("maxSize");
        if (StringUtils.hasText(maxSize)) {
            definitionBuilder.addPropertyValue("maxSize", maxSize);
            definitionBuilder.addPropertyValue("maxSize", maxSize);
        }

        String idColumn = element.getAttribute("idColumn");
        if (StringUtils.hasText(idColumn)) {
            definitionBuilder.addPropertyValue("idColumn", idColumn);
        }

        String eventColumn = element.getAttribute("eventColumn");
        if (StringUtils.hasText(eventColumn)) {
            definitionBuilder.addPropertyValue("eventColumn", eventColumn);
        }

        String nodeIdColumn = element.getAttribute("nodeIdColumn");
        if (StringUtils.hasText(eventColumn)) {
            definitionBuilder.addPropertyValue("nodeIdColumn", nodeIdColumn);
        }

        String tableName = element.getAttribute("tableName");
        if (StringUtils.hasText(tableName)) {
            definitionBuilder.addPropertyValue("tableName", tableName);
        }

        String dbTool = element.getAttribute("dbtool");
        if (StringUtils.hasText(dbTool)) {
            definitionBuilder.addPropertyReference("dbTool", dbTool);
        } else {
            definitionBuilder.addPropertyReference("dbTool", "dbtool.DBTool");
        }

        String autoKey = element.getAttribute("autoKey");
        if (StringUtils.hasText(autoKey)) {
            definitionBuilder.addPropertyReference("autoKey", autoKey);
        } else {
            definitionBuilder.addPropertyReference("autoKey", "autokey.IAutoKey");
        }


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }

}
