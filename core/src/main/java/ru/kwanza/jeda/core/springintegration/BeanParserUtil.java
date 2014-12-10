package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Alexander Guzanov
 */
public class BeanParserUtil {

    public static void resolvePropertyValue(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addPropertyValue(propertyName, attribute);
        }
    }

    public static void resolveMandatoryPropertyValue(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addPropertyValue(propertyName, attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolvePropertyRef(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addPropertyReference(propertyName, attribute);
        }
    }

    public static void resolveMandatoryPropertyRef(String attributeName, String propertyName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addPropertyReference(propertyName, attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveConstructorValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addConstructorArgValue(attribute);
        }
    }

    public static void resolveMandatoryConstructorValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addConstructorArgValue(attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }

    public static void resolveConstructoryRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addConstructorArgReference(attribute);
        }
    }

    public static void resolveMandatoryConstructorRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        final String attribute = element.getAttribute(attributeName);
        if (!StringUtils.hasText(attribute)) {
            builder.addConstructorArgReference(attribute);
        } else {
            throw new IllegalStateException("Expected mandatory attribute '" + attributeName + "'");
        }
    }


    public static void resolvePropertyValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolvePropertyValue(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyValue(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolveMandatoryPropertyValue(attributeName, attributeName, element, builder);
    }

    public static void resolvePropertyRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolvePropertyRef(attributeName, attributeName, element, builder);
    }

    public static void resolveMandatoryPropertyRef(String attributeName, Element element, BeanDefinitionBuilder builder) {
        resolveMandatoryPropertyRef(attributeName, attributeName, element, builder);
    }
}
