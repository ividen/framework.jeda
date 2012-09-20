package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.core.springintegration.CustomBeanDefinitionParserDelegate;
import ru.kwanza.jeda.nio.server.http.IEntryPointKeystore;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class CustomKeystoreParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return new CustomBeanDefinitionParserDelegate(parserContext)
                .parseBeanDefinition(element, null, IEntryPointKeystore.class).getBeanDefinition();
    }

}