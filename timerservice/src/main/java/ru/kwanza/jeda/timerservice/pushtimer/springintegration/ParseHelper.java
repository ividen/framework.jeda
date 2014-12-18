package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class ParseHelper {


    public static void parseChildren(Element element, ParserContext parserContext,BeanBuilder beanBuilder){
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();
        List<Element> childElements = DomUtils.getChildElements(element);
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
    }

    public static Map<Class, JedaBeanDefinition> parseChildren(Element element, ParserContext parserContext, final List<Class> acceptedClasses){
        final Map<Class, JedaBeanDefinition> result = new HashMap<Class,JedaBeanDefinition>();

        parseChildren(element, parserContext, new BeanBuilder() {
            @Override
            public void addBean(JedaBeanDefinition bean) {
               for (Class current : acceptedClasses) {
                   if (current.isAssignableFrom(bean.getType())) {
                       Object prev = result.put(current, bean);
                       if (prev != null) {
                           throw new RuntimeException("Duplicate bean definition!");
                       }
                       return;
                   }
               }
               throw new RuntimeException("Unsupported bean definition!");
            }
        });

        return result;
    }

}
