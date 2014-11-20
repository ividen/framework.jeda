package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.CustomBeanDefinitionParserDelegate;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;

/**
 * @author Alexander Guzanov
 */
class HelperBeanParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return new CustomBeanDefinitionParserDelegate(parserContext)
                .parseBeanDefinition(element, null, IDBQueueHelper.class).getBeanDefinition();
    }

}

