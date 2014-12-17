package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IStage;
import ru.kwanza.jeda.api.internal.IResourceController;
import ru.kwanza.jeda.api.internal.IThreadManager;
import ru.kwanza.jeda.api.timerservice.pushtimer.manager.ITimerCreator;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class TimerClassParser  extends JedaBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String name = element.getAttribute("name");
        if (name == null || name.isEmpty()) {
            String timerName = ((Element)element.getParentNode()).getAttribute(name);
            name = "CLASS" + "_" + timerName.toUpperCase(); //use timerName part of timerClassName for inline class definitions
        }

        Map<Class, JedaBeanDefinition> childBeanDef = ParseHelper.parseChildren(element, parserContext, Arrays.asList(IDBTimerDAO.class, ConsumerConfig.class));

        JedaBeanDefinition daoDef = childBeanDef.get(IDBTimerDAO.class);
        JedaBeanDefinition consumerConfigDef = childBeanDef.get(ConsumerConfig.class);
        if (consumerConfigDef == null) {
            consumerConfigDef = (JedaBeanDefinition)parserContext.getRegistry().getBeanDefinition("timerservice.default.DefaultConsumerConfig");
        }

        BeanDefinitionBuilder definitionBuilder= BeanDefinitionBuilder.genericBeanDefinition(TimerClass.class);

        definitionBuilder.addPropertyValue("timerClassName", name);
        definitionBuilder.addPropertyReference("dbTimerDAO", daoDef.getId());
        definitionBuilder.addPropertyReference("consumerConfig", consumerConfigDef.getId());

        return createJedaDefinition(name, definitionBuilder.getBeanDefinition(), TimerClass.class, element, parserContext);

    }
}
