package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

abstract class AbstractJDBCContextControllerParser extends JedaBeanDefinitionParser {

    protected static final String MANAGER = "manager";
    protected static final String DBTOOL = "dbTool";
    protected static final String VERSION_GENERATOR = "versionGenerator";
    protected static final String TERMINATOR = "terminator";
    protected static final String TABLE_NAME = "tableName";
    protected static final String ID_COLUMN_NAME = "idColumnName";
    protected static final String VERSION_COLUMN_NAME = "versionColumnName";
    protected static final String TERMINATOR_COLUMN_NAME = "terminatorColumnName";

    protected void addSimplePropertyValue(BeanDefinitionBuilder definitionBuilder, Element element, String propName) {
        String value = element.getAttribute(propName);
        if (StringUtils.hasText(value)) {
            definitionBuilder.addPropertyValue(propName, value);
        }
    }

}
