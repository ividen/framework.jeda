package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.nio.server.http.IEntryPointKeystore;
import ru.kwanza.jeda.nio.server.http.JKSEntryPointKeystore;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author: Guzanov Alexander
 */
class JKSEntryPointKeystoreParser extends JedaBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(JKSEntryPointKeystore.class);

        String keystoreFile = element.getAttribute("keystoreFile");
        definitionBuilder.addConstructorArgValue(keystoreFile);

        String keystorePassword = element.getAttribute("keystorePassword");
        definitionBuilder.addConstructorArgValue(keystorePassword);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IEntryPointKeystore.class, element, parserContext);
    }
}
