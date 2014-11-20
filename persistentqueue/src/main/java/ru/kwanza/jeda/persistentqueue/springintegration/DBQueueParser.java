package ru.kwanza.jeda.persistentqueue.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.persistentqueue.db.IDBQueueHelper;

import java.util.List;

/**
 * @author Guzanov Alexander
 */
class DBQueueParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(getFactoryBeanClass());

        List<Element> childElements = DomUtils.getChildElements(element);
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();

        String maxSize = element.getAttribute("maxSize");
        if (StringUtils.hasText(maxSize)) {
            definitionBuilder.addPropertyValue("maxSize", maxSize);
        }


        definitionBuilder.addPropertyReference("manager", "jeda.IJedaManager");
        definitionBuilder.addPropertyReference("em", "dbtool.IEntityManager");
        definitionBuilder.addPropertyReference("clusterService", "jeda.clusterservice.DBClusterService");


        for (Element e : childElements) {
            String namespaceURI = e.getNamespaceURI();
            NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
            if (handler == null) {
                readerContext.error("Unable to locate Spring NamespaceHandler" +
                        " for XML schema namespace [" + namespaceURI + "]", e);
            } else {
                BeanDefinition bean = handler.parse(e, parserContext);
                if (bean instanceof JedaBeanDefinition) {
                    final JedaBeanDefinition jedaBean = (JedaBeanDefinition) bean;
                    if (jedaBean.getType() == IDBQueueHelper.class) {
                        definitionBuilder.addPropertyReference("helper", jedaBean.getId());
                    }
                }
            }
        }


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }

    protected Class<DBQueueFactory> getFactoryBeanClass() {
        return DBQueueFactory.class;
    }

}
