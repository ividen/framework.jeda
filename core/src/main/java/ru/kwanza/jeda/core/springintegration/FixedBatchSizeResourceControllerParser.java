package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.core.resourcecontroller.FixedBatchSizeResourceController;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author: Guzanov Alexander
 */
class FixedBatchSizeResourceControllerParser extends FlexFlowBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(FixedBatchSizeResourceController.class);

        String batchSize = element.getAttribute("batchSize");
        definitionBuilder.addConstructorArgValue(batchSize);


        String waitForFillingTimeout = element.getAttribute("waitForFillingTimeout");
        if (StringUtils.hasText(waitForFillingTimeout)) {
            definitionBuilder.addPropertyValue("waitForFillingTimeout", waitForFillingTimeout);
        }

        String adjustmentCount = element.getAttribute("adjustmentCount");
        if (StringUtils.hasText(adjustmentCount)) {
            definitionBuilder.addPropertyValue("adjustmentCount", adjustmentCount);
        }

        String maxThreadCount = element.getAttribute("maxThreadCount");
        if (StringUtils.hasText(maxThreadCount)) {
            definitionBuilder.addPropertyValue("maxThreadCount", maxThreadCount);
        }

        String adjustmentInterval = element.getAttribute("adjustmentInterval");
        if (StringUtils.hasText(adjustmentInterval)) {
            definitionBuilder.addPropertyValue("adjustmentInterval", adjustmentInterval);
        }

        return createFlexFlowDefinition(definitionBuilder.getBeanDefinition(),
                IResourceController.class, element, parserContext);
    }
}

