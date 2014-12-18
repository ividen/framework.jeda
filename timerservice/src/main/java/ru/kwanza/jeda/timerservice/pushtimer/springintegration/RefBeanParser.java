package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;

/**
 * @author Michael Yeskov
 */
public class RefBeanParser extends JedaBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

        String ref = element.getAttribute("ref");
        if (!ref.isEmpty()) {
            return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition(ref);
        }

        throw new RuntimeException("ref attribute is required for " + element.getNodeName());
    }
}
