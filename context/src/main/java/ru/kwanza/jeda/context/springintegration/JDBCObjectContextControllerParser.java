package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.context.jdbc.JDBCObjectContextController;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

class JDBCObjectContextControllerParser extends AbstractJDBCContextControllerParser {

    private static final String CLAZZ = "clazz";
    private static final String TABLE_COLUMN_BY_PROPERTY_NAME = "tableColumnByPropertyName";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(JDBCObjectContextControllerFactory.class);

        addSimplePropertyValue(definitionBuilder, element, CLAZZ);

        definitionBuilder.addPropertyReference(TABLE_COLUMN_BY_PROPERTY_NAME, element.getAttribute(TABLE_COLUMN_BY_PROPERTY_NAME));
        definitionBuilder.addPropertyReference(MANAGER, ISystemManager.class.getName());

        String dbTool = element.getAttribute(DBTOOL);
        if (StringUtils.hasText(dbTool)) {
            definitionBuilder.addPropertyReference(DBTOOL, dbTool);
        } else {
            definitionBuilder.addPropertyReference(DBTOOL, "dbtool.DBTool");
        }

        String versionGenerator = element.getAttribute(VERSION_GENERATOR);
        if (StringUtils.hasText(versionGenerator)) {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, versionGenerator);
        } else {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, "dbtool.VersionGenerator");
        }

        addSimplePropertyValue(definitionBuilder, element, TERMINATOR);
        addSimplePropertyValue(definitionBuilder, element, TABLE_NAME);
        addSimplePropertyValue(definitionBuilder, element, ID_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, VERSION_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, TERMINATOR_COLUMN_NAME);


        return createJedaDefinition(definitionBuilder.getBeanDefinition(), JDBCObjectContextController.class, element, parserContext);
    }

}
