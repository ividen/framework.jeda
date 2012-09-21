package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.core.springintegration.SystemFlowBusFactory;
import ru.kwanza.jeda.nio.client.ClientTransportFlowBus;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class ClientTransportParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(ClientTransportFlowBus.class);
        definitionBuilder.addPropertyReference("manager", ISystemManager.class.getName());

        String name = element.getAttribute("name");
        definitionBuilder.addPropertyValue("name", name);

        String connectionPoolSizeObserver = element.getAttribute("connectionPoolSizeObserver");
        if (StringUtils.hasText(connectionPoolSizeObserver)) {
            definitionBuilder.addPropertyReference("connectionPoolSizeObserver", connectionPoolSizeObserver);
        }

        String directionQueueFactory = element.getAttribute("directionQueueFactory");
        if (StringUtils.hasText(directionQueueFactory)) {
            definitionBuilder.addPropertyReference("directionQueueFactory", directionQueueFactory);
        }

        String transport = element.getAttribute("transport");
        if (StringUtils.hasText(transport)) {
            definitionBuilder.addPropertyReference("transport", transport);
        }

        String threadCount = element.getAttribute("threadCount");
        if (StringUtils.hasText(threadCount)) {
            definitionBuilder.addPropertyValue("threadCount", threadCount);
        }


        definitionBuilder.setInitMethodName("init");
        final JedaBeanDefinition originalBean = createFlexFlowDefinition(definitionBuilder.getBeanDefinition(),
                IFlowBus.class, element, parserContext);

        parserContext.getReaderContext().getRegistry().registerBeanDefinition(originalBean.getId(), originalBean);
        BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(SystemFlowBusFactory.class);
        factoryBuilder.addPropertyReference("manager", ISystemManager.class.getName());
        factoryBuilder.addPropertyReference("original", originalBean.getId());
        JedaBeanDefinition result = new JedaBeanDefinition(name, IResourceController.class, factoryBuilder.getBeanDefinition());
        parserContext.getReaderContext().getRegistry().registerBeanDefinition(result.getId(), result);

        return result;
    }
}
