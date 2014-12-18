package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;

/**
 * @author Michael Yeskov
 */
public class RefBeanParser extends JedaBeanDefinitionParser {

    private Class refHolderClass;

    public RefBeanParser(Class refHolderClass) {
        this.refHolderClass = refHolderClass;
    }

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

        String ref = element.getAttribute("ref");
        if (!ref.isEmpty()) {
            BeanDefinitionBuilder definitionBuilder= BeanDefinitionBuilder.genericBeanDefinition(refHolderClass);
            definitionBuilder.addPropertyValue("ref", ref);
            return createJedaDefinition(definitionBuilder.getBeanDefinition(), refHolderClass, element, parserContext);
        }

        throw new RuntimeException("ref attribute is required for " + element.getNodeName());
    }
}
