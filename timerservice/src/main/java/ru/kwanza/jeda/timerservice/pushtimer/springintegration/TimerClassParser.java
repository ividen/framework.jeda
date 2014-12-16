package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;

/**
 * @author Michael Yeskov
 */
public class TimerClassParser  extends JedaBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

        String ref = element.getAttribute("ref");
        if (ref != null) {
           return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition(ref);
        }

        return null;
    }
}
