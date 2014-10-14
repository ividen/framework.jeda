package ru.kwanza.jeda.persistentqueue.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.core.springintegration.CustomBeanDefinitionParserDelegate;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;
import ru.kwanza.jeda.persistentqueue.PersistentQueue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author Guzanov Alexander
 */
class PersistentQueueParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(PersistentQueue.class);
        definitionBuilder.addConstructorArgReference(IJedaManager.class.getName());

        definitionBuilder.addConstructorArgValue(element.getAttribute("maxSize"));


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
                if ("persistence-controller".equals(e.getLocalName())) {
                    CustomBeanDefinitionParserDelegate delegate =
                            new CustomBeanDefinitionParserDelegate(parserContext);
                    JedaBeanDefinition jedaBeanDefinition = (JedaBeanDefinition) delegate
                            .parseBeanDefinition(e, null, IQueuePersistenceController.class).getBeanDefinition();
                    definitionBuilder.addConstructorArgReference(jedaBeanDefinition.getId());
                } else {
                    readerContext.error("Wrong definition of persistent queue", e);
                }
            }
        }


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);

    }
}
