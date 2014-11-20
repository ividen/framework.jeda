package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;
import ru.kwanza.jeda.persistentqueue.db.queue.NamedEventQueue;

/**
 * @author Alexander Guzanov
 */
class NamedEventQueueHelperParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(NamedEventQueue.Helper.class);

        definitionBuilder.addConstructorArgValue(element.getAttribute("queueName"));

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IDBQueueHelper.class, element, parserContext);
    }
}