package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.context.berkeley.BerkeleyBlobContextController;
import ru.kwanza.jeda.context.dictionary.dbinteractor.BerkeleyDictionaryDbInteractor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class BerkeleyBlobContextControllerWithDictParser extends BerkeleyBlobContextControllerParser {

    private static final String DB_INTERACTOR = "dbInteractor";
    private static final String DICT_TABLE_NAME = "dictionaryTableName";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(BerkeleyBlobContextControllerWithDictFactory.class);

        definitionBuilder.addPropertyReference(MANAGER, IJedaManager.class.getName());

        definitionBuilder.addPropertyReference(DB_INTERACTOR, BerkeleyDictionaryDbInteractor.class.getName());

        setSimplePropertyValue(definitionBuilder, element, DICT_TABLE_NAME);

        setSimplePropertyValue(definitionBuilder, element, DATABASE_NAME);

        String connectionFactory = element.getAttribute(CONNECTION_FACTORY);
        definitionBuilder.addPropertyReference(CONNECTION_FACTORY, connectionFactory);

        String versionGenerator = element.getAttribute(VERSION_GENERATOR);
        if (StringUtils.hasText(versionGenerator)) {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, versionGenerator);
        } else {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, "dbtool.VersionGenerator");
        }

        setSimplePropertyValue(definitionBuilder, element, TERMINATOR);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), BerkeleyBlobContextController.class, element, parserContext);
    }

}
