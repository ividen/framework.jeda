package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.TimerMapping;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class MappingParser  extends JedaBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String tableName = ((Element)element.getParentNode()).getAttribute("tableName");

        BeanDefinitionBuilder definitionBuilder =  BeanDefinitionBuilder.genericBeanDefinition(TimerMapping.class);

        List<Element> childElements = DomUtils.getChildElements(element);
        for (Element e : childElements) {
            String tagName = e.getLocalName();
            String fieldValue = e.getTextContent();

            definitionBuilder.addPropertyValue(toPropertyName(tagName), fieldValue);
        }

        if (tableName.isEmpty()) {
            throw new RuntimeException("tableName is required attribute");
        }
        definitionBuilder.addPropertyValue("tableName", tableName);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), TimerMapping.class, element, parserContext );
    }

    private String toPropertyName(String tagName) {
        switch (tagName) {
            case "id_field" : return "idField";
            case "state_field" : return "stateField";
            case "bucket_id_field" : return "bucketIdField";
            case "expire_time_field" : return "expireTimeField";
            case "creation_point_count_field" : return "creationPointCountField";
            default: throw new RuntimeException("unknown mapping tag name "+ tagName);
        }
    }


}
