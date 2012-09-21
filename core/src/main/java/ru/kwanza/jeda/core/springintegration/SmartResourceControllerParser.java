package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.core.resourcecontroller.SmartResourceController;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class SmartResourceControllerParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(SmartResourceController.class);

        String startBatchSize = element.getAttribute("startBatchSize");
        if (StringUtils.hasText(startBatchSize)) {
            definitionBuilder.addConstructorArgValue(startBatchSize);
        }

        String waitForFillingTimeout = element.getAttribute("waitForFillingTimeout");
        if (StringUtils.hasText(waitForFillingTimeout)) {
            definitionBuilder.addPropertyValue("waitForFillingTimeout", waitForFillingTimeout);
        }

        String adjustmentCount = element.getAttribute("adjustmentCount");
        if (StringUtils.hasText(adjustmentCount)) {
            definitionBuilder.addPropertyValue("adjustmentCount", adjustmentCount);
        }

        String adjustmentInterval = element.getAttribute("adjustmentInterval");
        if (StringUtils.hasText(adjustmentInterval)) {
            definitionBuilder.addPropertyValue("adjustmentInterval", adjustmentInterval);
        }

        String processingTimeThreshold = element.getAttribute("processingTimeThreshold");
        if (StringUtils.hasText(processingTimeThreshold)) {
            definitionBuilder.addPropertyValue("processingTimeThreshold", processingTimeThreshold);
        }

        String maxThreadCount = element.getAttribute("maxThreadCount");
        if (StringUtils.hasText(maxThreadCount)) {
            definitionBuilder.addPropertyValue("maxThreadCount", maxThreadCount);
        }

        String maxElementCount = element.getAttribute("maxElementCount");
        if (StringUtils.hasText(maxElementCount)) {
            definitionBuilder.addPropertyValue("maxElementCount", maxElementCount);
        }

        String maxBatchSize = element.getAttribute("maxBatchSize");
        if (StringUtils.hasText(maxBatchSize)) {
            definitionBuilder.addPropertyValue("maxBatchSize", maxBatchSize);
        }

        return createFlexFlowDefinition(definitionBuilder.getBeanDefinition(),
                IResourceController.class, element, parserContext);
    }
}
