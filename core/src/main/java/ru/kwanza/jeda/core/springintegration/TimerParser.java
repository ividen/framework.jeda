package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.ITimer;
import ru.kwanza.jeda.api.ISystemManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class TimerParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        JedaBeanDefinition originalBean = new CustomBeanDefinitionParserDelegate(parserContext)
                .parseBeanDefinitionElement(element, null, ITimer.class);

        BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(SystemTimerFactory.class);
        factoryBuilder.addPropertyReference("manager", ISystemManager.class.getName());
        factoryBuilder.addPropertyReference("original", originalBean.getId());
        JedaBeanDefinition result = new JedaBeanDefinition(element.getAttribute("name"),
                ITimer.class, factoryBuilder.getBeanDefinition());
        parserContext.getReaderContext().getRegistry().registerBeanDefinition(result.getId(), result);

        return result;
    }

}
