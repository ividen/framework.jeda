package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import com.sun.tools.internal.xjc.api.Mapping;
import org.springframework.beans.PropertyValue;
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
import ru.kwanza.jeda.timerservice.entitytimer.TimerMapping;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.ConsumerConfig;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.InsertDeleteDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.InsertMultiUpdateDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.InsertSingleUpdateDBTimerDAO;
import ru.kwanza.jeda.timerservice.pushtimer.dao.basis.UpdatingDBTimerDAO;
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
        String timerName = ((Element)element.getParentNode().getParentNode()).getAttribute("name");

        Class daoClass = getDAOClass(element.getNodeName());

        JedaBeanDefinition handleMapperDef = getHandleMapperDef(element, parserContext, timerName);
        JedaBeanDefinition timerMappingDef = getTimerMappingDef(element, parserContext, timerName);

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

        return new JedaBeanDefinition("DAO_"  + timerName, IDBTimerDAO.class, defBuilder.getBeanDefinition());
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

    private JedaBeanDefinition getTimerMappingDef(Element element, ParserContext parserContext, String timerName) {
        String tableName = element.getAttribute("tableName");
        if (tableName.isEmpty()) {
            throw new RuntimeException("tableName is required property for DAO");
        }

        Map<Class, JedaBeanDefinition> childBeanDef = ParseHelper.parseChildren(element, parserContext, (List) Arrays.asList(TimerMapping.class));
        JedaBeanDefinition timerMappingDef = childBeanDef.get(TimerMapping.class);

        if (timerMappingDef == null) {
            BeanDefinitionBuilder defBuilderMapping= BeanDefinitionBuilder.genericBeanDefinition(TimerMapping.class);
            defBuilderMapping.addPropertyValue("tableName", tableName);
            timerMappingDef = new JedaBeanDefinition( "TIMER_MAPPING_" + timerName.toUpperCase(), TimerMapping.class, defBuilderMapping.getBeanDefinition());
            parserContext.getRegistry().registerBeanDefinition(timerMappingDef.getId(), timerMappingDef);
        }
        return timerMappingDef;
    }

    private JedaBeanDefinition getHandleMapperDef(Element element, ParserContext parserContext, String timerName) {
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
        JedaBeanDefinition result = new JedaBeanDefinition("HANDLE_MAPPER_" + timerName.toUpperCase() , ITimerHandleMapper.class, defBuilderHandleMapper.getBeanDefinition());
        parserContext.getRegistry().registerBeanDefinition(result.getId(), result);
        return result;
    }


}
