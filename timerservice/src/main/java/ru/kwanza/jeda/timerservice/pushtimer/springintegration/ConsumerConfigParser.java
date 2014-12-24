package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Yeskov
 */
public class ConsumerConfigParser extends JedaBeanDefinitionParser {

    private static final List<String> attributes = Arrays.asList("workerCount", "borderGain",
            "idealWorkingInterval", "firedTimersMaxLimit", "firedTimersSingleConsumerModeLimit",
            "firedTimersAgainMultiConsumerBorder");

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConfig.class);


        for (String current : attributes) {
            String value = element.getAttribute(current);
            if (!value.isEmpty()) {
                definitionBuilder.addPropertyValue(current, value);
            }
        }

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), ConsumerConfig.class, element, parserContext);
    }
}
