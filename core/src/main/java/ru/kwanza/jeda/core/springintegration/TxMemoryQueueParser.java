package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IQueue;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class TxMemoryQueueParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TransactionalMemoryQueue.class);
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");
        String sType = element.getAttribute("cloneType");
        ObjectCloneType type = ObjectCloneType.SERIALIZE;
        if (StringUtils.hasText(sType)) {
            if ("SERIALIZE".equals(sType)) {
                type = ObjectCloneType.SERIALIZE;
            } else if ("CLONE".equals(sType)) {
                type = ObjectCloneType.CLONE;
            } else if("NONE".equals(sType)){
                type = ObjectCloneType.NONE;
            }
        }

        definitionBuilder.addConstructorArgValue(type);

        String maxSize = element.getAttribute("maxSize");
        if (StringUtils.hasText(maxSize)) {
            definitionBuilder.addConstructorArgValue(maxSize);
        } else {
            definitionBuilder.addConstructorArgValue(Long.MAX_VALUE);
        }

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), IQueue.class, element, parserContext);
    }
}