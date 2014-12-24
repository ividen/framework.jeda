package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.springintegration.refs.ConsumerConfigRef;
import ru.kwanza.jeda.timerservice.pushtimer.springintegration.refs.DAORef;
import ru.kwanza.jeda.timerservice.pushtimer.springintegration.refs.RefHolder;

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
        if (name.isEmpty()) {
            String timerName = ((Element)element.getParentNode()).getAttribute("name");
            name = "CLASS_"  + timerName.toUpperCase(); //use timerName part of timerClassName for inline class definitions
        }

        Map<Class, JedaBeanDefinition> childBeanDef = ParseHelper.parseChildren(element, parserContext, (List)Arrays.asList(IDBTimerDAO.class, ConsumerConfig.class, DAORef.class, ConsumerConfigRef.class));
        JedaBeanDefinition daoDef = childBeanDef.get(IDBTimerDAO.class);
        JedaBeanDefinition customDaoRefDef = childBeanDef.get(DAORef.class);
        JedaBeanDefinition consumerConfigDef = childBeanDef.get(ConsumerConfig.class);
        JedaBeanDefinition refConsumerConfigDef = childBeanDef.get(ConsumerConfigRef.class);


        BeanDefinitionBuilder definitionBuilder= BeanDefinitionBuilder.genericBeanDefinition(TimerClass.class);
        definitionBuilder.addPropertyValue("timerClassName", name);

        String consumerConfigId = "jeda.timerservice.default.DefaultConsumerConfig";
        if (consumerConfigDef != null) {
            consumerConfigId = consumerConfigDef.getId();
        } else if (refConsumerConfigDef != null) {
            consumerConfigId = (String)refConsumerConfigDef.getPropertyValues().getPropertyValue("ref").getValue();
        }
        definitionBuilder.addPropertyReference("consumerConfig", consumerConfigId);

        String daoId;
        if (daoDef != null) {
            daoId = daoDef.getId();
        } else if (customDaoRefDef != null) {
            daoId = (String)customDaoRefDef.getPropertyValues().getPropertyValue("ref").getValue();
        } else {
            throw new RuntimeException("dao must be declared for timer class " + name);
        }
        definitionBuilder.addPropertyReference("dbTimerDAO", daoId);


        return createJedaDefinition(name, definitionBuilder.getBeanDefinition(), TimerClass.class, element, parserContext);

    }
}
