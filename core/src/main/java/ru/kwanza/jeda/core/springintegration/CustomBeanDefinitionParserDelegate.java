package ru.kwanza.jeda.core.springintegration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.*;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
public class CustomBeanDefinitionParserDelegate extends BeanDefinitionParserDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanDefinitionParserDelegate.class);
    private final ParserContext parserContext;

    public CustomBeanDefinitionParserDelegate(ParserContext parserContext) {
        super(parserContext.getReaderContext());
        this.parserContext = parserContext;
    }

    public BeanDefinitionHolder parseBeanDefinition(Element ele,
                                                    BeanDefinition containingBean,
                                                    Class type) {
        String id = ele.getAttribute(ID_ATTRIBUTE);
        String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

        return parse(ele, containingBean, type, id, nameAttr);
    }

    private BeanDefinitionHolder parse(Element ele, BeanDefinition containingBean, Class type, String id, String nameAttr) {
        final JedaBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, containingBean, type, id, nameAttr);
        final BeanDefinitionHolder result = new BeanDefinitionHolder(beanDefinition, beanDefinition.getId(), null);
        final XmlReaderContext readerContext = getReaderContext();
        final NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();

        NamedNodeMap attributes = ele.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            String namespaceURI = item.getNamespaceURI();
            if (namespaceURI != null && !namespaceURI.equals(ele.getNamespaceURI())) {
                NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
                if (handler == null) {
                    readerContext.error("Unable to locate Spring NamespaceHandler" +
                            " for XML schema namespace [" + namespaceURI + "]", ele);
                } else {
                    handler.decorate(item, result, parserContext);
                }
            }
        }


        return result;
    }

    public JedaBeanDefinition parseBeanDefinitionElement(Element ele, BeanDefinition containingBean,
                                                     Class type) {
        return (JedaBeanDefinition) parse(ele, containingBean, type, null, null).getBeanDefinition();
    }

    private JedaBeanDefinition parseBeanDefinitionElement(Element ele, BeanDefinition containingBean,
                                                             Class type, String id, String nameAttr) {
        List<String> aliases = new ArrayList<String>();
        if (StringUtils.hasLength(nameAttr)) {
            String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS);
            aliases.addAll(Arrays.asList(nameArr));
        }

        String beanName = id;
        if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
            beanName = aliases.remove(0);
            if (logger.isDebugEnabled()) {
                logger.debug("No XML 'id' specified - using '" + beanName
                        + "' as bean name and " + aliases + " as aliases");
            }
        }

        if (containingBean == null) {
            checkNameUniqueness(beanName, aliases, ele);
        }

        AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
        if (beanDefinition != null) {
            final XmlReaderContext readerContext = getReaderContext();
            final BeanDefinitionRegistry registry = readerContext.getRegistry();
            final NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();

            if (!StringUtils.hasText(beanName)) {
                try {
                    if (containingBean != null) {
                        beanName = BeanDefinitionReaderUtils.generateBeanName(
                                beanDefinition, registry, true);
                    } else {
                        beanName = readerContext.generateBeanName(beanDefinition);
                        String beanClassName = beanDefinition.getBeanClassName();
                        if (beanClassName != null
                                && beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length()
                                && !registry.isBeanNameInUse(beanClassName)) {
                            aliases.add(beanClassName);
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Neither XML 'id' nor 'name' specified - "
                                + "using generated bean name [" + beanName + "]");
                    }
                } catch (Exception ex) {
                    error(ex.getMessage(), ele);
                    return null;
                }
            }

            JedaBeanDefinition result = new JedaBeanDefinition(beanName, type, beanDefinition);
            getReaderContext().getRegistry().registerBeanDefinition(beanName, result);
            return result;
        }

        return null;
    }
}
