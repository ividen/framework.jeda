package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;
import ru.kwanza.jeda.persistentqueue.db.queue.PriorityEventQueue;

/**
 * @author Alexander Guzanov
 */
class PriorityEventQueueHelperParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(PriorityEventQueue.Helper.class);


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IDBQueueHelper.class, element, parserContext);
    }
}