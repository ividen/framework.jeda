package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.internal.ISystemManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class FlowBusParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        JedaBeanDefinition originalBean = new CustomBeanDefinitionParserDelegate(parserContext)
                .parseBeanDefinitionElement(element, null, IFlowBus.class);

        BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(SystemFlowBusFactory.class);
        factoryBuilder.addPropertyReference("manager", ISystemManager.class.getName());
        factoryBuilder.addPropertyReference("original", originalBean.getId());
        JedaBeanDefinition result = new JedaBeanDefinition(element.getAttribute("name"), IFlowBus.class,
                factoryBuilder.getBeanDefinition());
        parserContext.getReaderContext().getRegistry().registerBeanDefinition(result.getId(), result);

        return result;
    }

}

