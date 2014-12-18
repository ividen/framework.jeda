package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.*;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.ITimerHandleMapper;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.LongConstantNameTimerHandleMapper;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.LongNameSetTimerHandleMapper;
import ru.kwanza.jeda.timerservice.pushtimer.dao.handle.StringUniversalTimerHandleMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Yeskov
 */
public class DAOParser  extends JedaBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String name = ((Element)element.getParentNode()).getAttribute("name").toUpperCase();
        if (name.isEmpty()) {
            name = ((Element)element.getParentNode().getParentNode()).getAttribute("name").toUpperCase();
        }
        if (name.isEmpty()) {
            throw new RuntimeException("Can't find name");
        }

        Class daoClass = getDAOClass(element.getLocalName());

        JedaBeanDefinition handleMapperDef = getHandleMapperDef(element, parserContext, name);
        JedaBeanDefinition timerMappingDef = getTimerMappingDef(element, parserContext, name);

        String fetchSize = element.getAttribute("fetchSize");
        String useOracleOptimizedFetchCursor = element.getAttribute("useOracleOptimizedFetchCursor");

        BeanDefinitionBuilder defBuilder = BeanDefinitionBuilder.genericBeanDefinition(daoClass);
        defBuilder.addPropertyValue("handleMapper", handleMapperDef);
        defBuilder.addPropertyValue("mapping", timerMappingDef);
        if (!fetchSize.isEmpty()) {
            defBuilder.addPropertyValue("fetchSize", fetchSize);
        }
        if (!useOracleOptimizedFetchCursor.isEmpty()) {
            defBuilder.addPropertyValue("useOracleOptimizedFetchCursor", useOracleOptimizedFetchCursor);
        }

        return new JedaBeanDefinition("DAO_" + name, IDBTimerDAO.class, defBuilder.getBeanDefinition());
    }

    private Class getDAOClass(String nodeName) {
        switch (nodeName) {
            case "dao-insert-delete":
                return InsertDeleteDBTimerDAO.class;
            case "dao-insert-single-update":
                return InsertSingleUpdateDBTimerDAO.class;
            case "dao-insert-multi-update":
                return InsertMultiUpdateDBTimerDAO.class;
            case "dao-updating":
                return UpdatingDBTimerDAO.class;
            default:
                throw new RuntimeException("Unknown DAO type " + nodeName);
        }
    }

    private JedaBeanDefinition getTimerMappingDef(Element element, ParserContext parserContext, String name) {
        String tableName = element.getAttribute("tableName");
        if (tableName.isEmpty()) {
            throw new RuntimeException("tableName is required property for DAO");
        }

        Map<Class, JedaBeanDefinition> childBeanDef = ParseHelper.parseChildren(element, parserContext, (List) Arrays.asList(TimerMapping.class));
        JedaBeanDefinition timerMappingDef = childBeanDef.get(TimerMapping.class);

        if (timerMappingDef == null) {
            BeanDefinitionBuilder defBuilderMapping= BeanDefinitionBuilder.genericBeanDefinition(TimerMapping.class);
            defBuilderMapping.addPropertyValue("tableName", tableName);
            timerMappingDef = new JedaBeanDefinition( "TIMER_MAPPING_" + name, TimerMapping.class, defBuilderMapping.getBeanDefinition());
            parserContext.getRegistry().registerBeanDefinition(timerMappingDef.getId(), timerMappingDef);
        }
        return timerMappingDef;
    }

    private JedaBeanDefinition getHandleMapperDef(Element element, ParserContext parserContext, String name) {
        Class handleMapperClass;
        String handleMapper = element.getAttribute("handleMapper");
        if (!handleMapper.isEmpty()) {
            switch (handleMapper) {
                case "LongConstantName":
                    handleMapperClass = LongConstantNameTimerHandleMapper.class;
                    break;
                case "LongNameSet":
                    handleMapperClass = LongNameSetTimerHandleMapper.class;
                    break;
                case "StringUniversal":
                    handleMapperClass = StringUniversalTimerHandleMapper.class;
                    break;
                default:
                    throw new RuntimeException("Unknown handleMapper");
            }
        } else {
            throw new RuntimeException("handleMapper is required attribute for DAO");
        }
        BeanDefinitionBuilder defBuilderHandleMapper = BeanDefinitionBuilder.genericBeanDefinition(handleMapperClass);
        JedaBeanDefinition result = new JedaBeanDefinition("HANDLE_MAPPER_" + name , ITimerHandleMapper.class, defBuilderHandleMapper.getBeanDefinition());
        parserContext.getRegistry().registerBeanDefinition(result.getId(), result);
        return result;
    }


}
