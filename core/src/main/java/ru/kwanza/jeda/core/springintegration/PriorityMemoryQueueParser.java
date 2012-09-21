package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.core.queue.PriorityMemoryQueue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class PriorityMemoryQueueParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(PriorityMemoryQueue.class);
        String maxSize = element.getAttribute("maxSize");
        if (StringUtils.hasText(maxSize)) {
            definitionBuilder.addConstructorArgValue(maxSize);
        }
        return createFlexFlowDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }
}
