package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.SharedThreadManager;
import ru.kwanza.jeda.core.threadmanager.shared.comparator.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.*;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Comparator;
import java.util.List;

/**
 * @author Guzanov Alexander
 */
class SharedThreadManagerParser extends JedaBeanDefinitionParser {

    public static class InputRateStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(InputRateComparator.class);

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }


    public static class ThreadCountStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ThreadCountComparator.class);

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class WaitingTimeStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(WaitingTimeComparator.class);

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class InputRateAndWaitingTimeStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(InputRateAndWaitingTimeComparator.class);


            String maxWaitingTime = element.getAttribute("maxWaitingTime");
            if (StringUtils.hasText(maxWaitingTime)) {
                definitionBuilder.addPropertyValue("maxWaitingTime", maxWaitingTime);
            }

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class ThreadCountAndWaitingTimeStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ThreadCountAndWaitingTimeComparator.class);

            String maxWaitingTime = element.getAttribute("maxWaitingTime");
            if (StringUtils.hasText(maxWaitingTime)) {
                definitionBuilder.addPropertyValue("maxWaitingTime", maxWaitingTime);
            }

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class QueueSizeStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(QueueSizeComparator.class);

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class RoundRobinStrategyParser extends JedaBeanDefinitionParser {
        protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(RoundRobinComparator.class);

            return createJedaDefinition(definitionBuilder.getBeanDefinition(), Comparator.class, element, parserContext);
        }
    }

    public static class CustomStrategyParser implements BeanDefinitionParser {
        public BeanDefinition parse(Element element, ParserContext parserContext) {
            return new CustomBeanDefinitionParserDelegate(parserContext)
                    .parseBeanDefinition(element, null, Comparator.class).getBeanDefinition();
        }

    }


    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(SharedThreadManager.class);
        definitionBuilder.addConstructorArgValue(element.getAttribute("threadNamePrefix"));
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");

        String maxThreadCount = element.getAttribute("maxThreadCount");
        if (StringUtils.hasText(maxThreadCount)) {
            definitionBuilder.addPropertyValue("maxThreadCount", maxThreadCount);
        }

        String maxSingleEventAttempt = element.getAttribute("maxSingleEventAttempt");
        if (StringUtils.hasText(maxSingleEventAttempt)) {
            definitionBuilder.addPropertyValue("maxSingleEventAttempt", maxSingleEventAttempt);
        }


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
                    JedaBeanDefinition jedaBeanDefinition = (JedaBeanDefinition) bean;
                    if (jedaBeanDefinition.getType() == Comparator.class) {
                        definitionBuilder.addPropertyReference("stageComparator", jedaBeanDefinition.getId());
                    } else {
                        throw new RuntimeException("Unsupported element definition!");
                    }
                }
            }
        }


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IThreadManager.class, element, parserContext);
    }
}