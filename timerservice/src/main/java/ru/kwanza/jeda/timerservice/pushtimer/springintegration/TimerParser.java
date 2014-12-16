package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import ru.kwanza.jeda.api.IStage;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.api.timerservice.pushtimer.timer.ITimer;
import ru.kwanza.jeda.core.manager.SystemStage;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.timer.Timer;
import ru.kwanza.jeda.core.springintegration.SystemStageFactory;

import java.util.List;

/**
 * @author Michael Yeskov
 */
class TimerParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilderStageFactory = BeanDefinitionBuilder.genericBeanDefinition(TimerStageFactory.class);
        List<Element> childElements = DomUtils.getChildElements(element);
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();
        String name = element.getAttribute("name");


        TimerBeanBuilder beanBuilder = new TimerBeanBuilder(name, parserContext);
        for (Element e : childElements) {
            String namespaceURI = e.getNamespaceURI();
            NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
            if (handler == null) {
                readerContext.error("Unable to locate Spring NamespaceHandler" +
                        " for XML schema namespace [" + namespaceURI + "]", e);
            } else {
                BeanDefinition bean = handler.parse(e, parserContext);

                if (bean instanceof JedaBeanDefinition) {
                    beanBuilder.addBean((JedaBeanDefinition) bean);
                }
            }
        }

        beanBuilder.build(definitionBuilderStageFactory);
        return createJedaDefinition(name, definitionBuilderStageFactory.getBeanDefinition(), IStage.class, element, parserContext);
    }
}
