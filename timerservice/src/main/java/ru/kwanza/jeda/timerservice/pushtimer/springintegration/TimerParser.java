package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import ru.kwanza.jeda.api.IStage;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;

/**
 * @author Michael Yeskov
 */
class TimerParser extends JedaBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String name = element.getAttribute("name");

        TimerBeanBuilder beanBuilder = new TimerBeanBuilder(name, parserContext);

        ParseHelper.parseChildren(element, parserContext, beanBuilder);

        BeanDefinitionBuilder definitionBuilderStageFactory = BeanDefinitionBuilder.genericBeanDefinition(TimerStageFactory.class);
        beanBuilder.build(definitionBuilderStageFactory);
        return createJedaDefinition(name, definitionBuilderStageFactory.getBeanDefinition(), IStage.class, element, parserContext);
    }
}
