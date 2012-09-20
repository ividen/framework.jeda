package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class CustomQueueParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return new CustomBeanDefinitionParserDelegate(parserContext)
                .parseBeanDefinition(element, null, IQueue.class).getBeanDefinition();
    }

}
