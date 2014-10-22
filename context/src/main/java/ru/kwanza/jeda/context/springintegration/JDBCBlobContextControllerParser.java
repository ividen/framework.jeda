package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.context.jdbc.JDBCBlobContextController;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

class JDBCBlobContextControllerParser extends AbstractJDBCContextControllerParser {

    protected static final String CONTEXT_DATA_COLUMN_NAME = "contextDataColumnName";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(JDBCBlobContextControllerFactory.class);

        definitionBuilder.addPropertyReference(MANAGER, "jeda.IJedaManager");

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
        addSimplePropertyValue(definitionBuilder, element, CONTEXT_DATA_COLUMN_NAME);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), JDBCBlobContextController.class, element, parserContext);
    }

}
