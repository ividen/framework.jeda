package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class ThreadManagerParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String ref = element.getAttribute("ref");
        return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition(ref);
    }
}