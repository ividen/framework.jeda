package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Guzanov
 */
public class BeanParserUtil {

    public static void resolvePropertyValue(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyValue(propertyName, attribute);
        }
    }

    public static void resolveMandatoryPropertyValue(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyValue(propertyName, attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveMandatoryPropertyValue(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder, Object defaultValue) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyValue(propertyName, attribute);
        } else {
            builder.addPropertyValue(propertyName, defaultValue);
        }
    }

    public static void resolvePropertyRef(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyReference(propertyName, attribute);
        }
    }

    public static void resolveMandatoryPropertyRef(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyReference(propertyName, attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveMandatoryPropertyRef(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder, Object defaultValue) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addPropertyReference(propertyName, attribute);
        } else {
            builder.addPropertyValue(propertyName, defaultValue);
        }
    }

    public static void resolveConstructorValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgValue(attribute);
        }
    }

    public static void resolveMandatoryConstructorValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgValue(attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveMandatoryConstructorValue(String attributeName, Element element, BeanDefinitionBuilder builder, Object defaulValue) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgValue(attribute);
        } else {
            builder.addConstructorArgValue(defaulValue);
        }
    }

    public static void resolveConstructoryRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgReference(attribute);
        }
    }

    public static void resolveMandatoryConstructorRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgReference(attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveMandatoryConstructorRef(String attributeName, Element element, BeanDefinitionBuilder builder, Object defaultRef) {
        final String attribute = element.getAttribute(attributeName);
        if (StringUtils.hasText(attribute)) {
            builder.addConstructorArgReference(attribute);
        } else {
            builder.addConstructorArgValue(defaultRef);
        }
    }

    public static void resolvePropertyValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolvePropertyValue(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolveMandatoryPropertyValue(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyValue(String attributeName, Element element, BeanDefinitionBuilder builder, Object defaultValue) {
        resolveMandatoryPropertyValue(attributeName, attributeName, element, builder, defaultValue);
    }

    public static void resolvePropertyRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolvePropertyRef(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolveMandatoryPropertyRef(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyRef(String attributeName, Element element, BeanDefinitionBuilder builder, Object defaultValue) {
        resolveMandatoryPropertyRef(attributeName, attributeName, element, builder, defaultValue);
    }

    public static List<BeanDefinition> parseChildElements(Element element, ParserContext parserContext) {
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();
        List<Element> childElements = DomUtils.getChildElements(element);
        for (Element e : childElements) {
            String namespaceURI = e.getNamespaceURI();
            org.springframework.beans.factory.xml.NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
            if (handler == null) {
                readerContext.error("Unable to locate Spring NamespaceHandler" +
                        " for XML schema namespace [" + namespaceURI + "]", e);
            } else {
                result.add(handler.parse(e, parserContext));
            }
        }
        return result;
    }
}
