package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.context.dictionary.dbinteractor.JDBCDictionaryDbInteractor;
import ru.kwanza.jeda.context.jdbc.JDBCBlobContextControllerWithDictionary;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

class JDBCBlobContextControllerWithDictParser extends JDBCBlobContextControllerParser {
    
    private static final String DICT_TABLE_NAME = "dictionaryTableName";
    private static final String DICT_PROPERTY_COLUMN_NAME = "dictionaryPropertyColumnName";
    private static final String DICT_ID_COLUMN_NAME = "dictionaryIdColumnName";
    private static final String DB_INTERACTOR = "dbInteractor";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(JDBCBlobContextControllerWithDictFactory.class);

        definitionBuilder.addPropertyReference(DB_INTERACTOR, JDBCDictionaryDbInteractor.class.getName());

        addSimplePropertyValue(definitionBuilder, element, DICT_TABLE_NAME);
        addSimplePropertyValue(definitionBuilder, element, DICT_PROPERTY_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, DICT_ID_COLUMN_NAME);

        definitionBuilder.addPropertyReference(MANAGER, ISystemManager.class.getName());

        String dbTool = element.getAttribute(DBTOOL);
        if (StringUtils.hasText(dbTool)) {
            definitionBuilder.addPropertyReference(DBTOOL, dbTool);
        } else {
            definitionBuilder.addPropertyReference(DBTOOL, "dbhelper.DBTool");
        }

        String versionGenerator = element.getAttribute(VERSION_GENERATOR);
        if (StringUtils.hasText(versionGenerator)) {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, versionGenerator);
        } else {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, "dbhelper.VersionGenerator");
        }

        addSimplePropertyValue(definitionBuilder, element, TERMINATOR);
        addSimplePropertyValue(definitionBuilder, element, TABLE_NAME);
        addSimplePropertyValue(definitionBuilder, element, ID_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, VERSION_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, TERMINATOR_COLUMN_NAME);
        addSimplePropertyValue(definitionBuilder, element, CONTEXT_DATA_COLUMN_NAME);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), JDBCBlobContextControllerWithDictionary.class, element,
                parserContext);
    }


}
