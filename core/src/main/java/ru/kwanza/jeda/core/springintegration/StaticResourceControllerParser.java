package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.core.resourcecontroller.StaticResourceController;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class StaticResourceControllerParser extends FlexFlowBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(StaticResourceController.class);
        String threadCount = element.getAttribute("threadCount");
        if (StringUtils.hasText(threadCount)) {
            definitionBuilder.addPropertyValue("threadCount", threadCount);
        }

        String batchSize = element.getAttribute("batchSize");
        if (StringUtils.hasText(batchSize)) {
            definitionBuilder.addPropertyValue("batchSize", batchSize);
        }

        String adjustmentCount = element.getAttribute("adjustmentCount");
        if (StringUtils.hasText(adjustmentCount)) {
            definitionBuilder.addPropertyValue("adjustmentCount", adjustmentCount);
        }

        String adjustmentInterval = element.getAttribute("adjustmentInterval");
        if (StringUtils.hasText(adjustmentInterval)) {
            definitionBuilder.addPropertyValue("adjustmentInterval", adjustmentInterval);
        }

        return createFlexFlowDefinition(definitionBuilder.getBeanDefinition(),
                IResourceController.class, element, parserContext);
    }
}
