package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.nio.server.http.HttpServer;
import ru.kwanza.jeda.nio.server.http.IEntryPoint;
import ru.kwanza.jeda.nio.server.http.IHttpServer;
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

import java.util.List;

/**
 * @author Guzanov Alexander
 */
class HttpServerParser extends JedaBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(HttpServer.class);
        List<Element> childElements = DomUtils.getChildElements(element);
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();

        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");
        String name = element.getAttribute("name");
        definitionBuilder.addConstructorArgValue(name);

        String keepAliveIdleTimeout = element.getAttribute("keepAliveIdleTimeout");
        if (StringUtils.hasText(keepAliveIdleTimeout)) {
            definitionBuilder.addPropertyValue("keepAliveIdleTimeout", keepAliveIdleTimeout);
        }

        String keepAliveMaxRequestsCount = element.getAttribute("keepAliveMaxRequestsCount");
        if (StringUtils.hasText(keepAliveMaxRequestsCount)) {
            definitionBuilder.addPropertyValue("keepAliveMaxRequestsCount", keepAliveMaxRequestsCount);
        }

        definitionBuilder.setInitMethodName("init");
        definitionBuilder.setDestroyMethodName("destroy");
        JedaBeanDefinition result = createJedaDefinition(definitionBuilder.getBeanDefinition(), IHttpServer.class, element, parserContext);

        for (Element e : childElements) {
            String namespaceURI = e.getNamespaceURI();
            NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
            if (handler == null) {
                readerContext.error("Unable to locate Spring NamespaceHandler" +
                        " for XML schema namespace [" + namespaceURI + "]", e);
            } else {
                BeanDefinition bean = handler.parse(e, parserContext);
                if (bean instanceof JedaBeanDefinition) {
                    JedaBeanDefinition jedaBeanDefinition = (JedaBeanDefinition) bean;
                    if (jedaBeanDefinition.getType() == IEntryPoint.class) {
                        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RegistrationFactory.class);
                        builder.setFactoryMethod("registerEntryPoint");
                        builder.addConstructorArgReference(name);
                        builder.addConstructorArgReference(jedaBeanDefinition.getId());
                        AbstractBeanDefinition fakeBeanDefinition = builder.getBeanDefinition();
                        parserContext.getRegistry().registerBeanDefinition(
                                parserContext.getReaderContext().generateBeanName(fakeBeanDefinition), fakeBeanDefinition);
                    }
                }
            }
        }

        return result;
    }
}
