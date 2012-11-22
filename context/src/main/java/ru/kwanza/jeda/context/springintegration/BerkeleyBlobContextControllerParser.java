package ru.kwanza.jeda.context.springintegration;

import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.context.berkeley.BerkeleyBlobContextController;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

class BerkeleyBlobContextControllerParser extends JedaBeanDefinitionParser {

    protected static final String MANAGER = "manager";
    protected static final String DATABASE_NAME = "databaseName";
    protected static final String CONNECTION_FACTORY = "connectionFactory";
    protected static final String VERSION_GENERATOR = "versionGenerator";
    protected static final String TERMINATOR = "terminator";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(BerkeleyBlobContextControllerFactory.class);

        definitionBuilder.addPropertyReference(MANAGER, ISystemManager.class.getName());

        setSimplePropertyValue(definitionBuilder, element, DATABASE_NAME);

        String connectionFactory = element.getAttribute(CONNECTION_FACTORY);
        definitionBuilder.addPropertyReference(CONNECTION_FACTORY, connectionFactory);

        String versionGenerator = element.getAttribute(VERSION_GENERATOR);
        if (StringUtils.hasText(versionGenerator)) {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, versionGenerator);
        } else {
            definitionBuilder.addPropertyReference(VERSION_GENERATOR, "dbhelper.VersionGenerator");
        }

        setSimplePropertyValue(definitionBuilder, element, TERMINATOR);

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), BerkeleyBlobContextController.class, element, parserContext);
    }

    protected void setSimplePropertyValue(BeanDefinitionBuilder definitionBuilder, Element element, String propName) {
        String value = element.getAttribute(propName);
        if (StringUtils.hasText(value)) {
            definitionBuilder.addPropertyValue(propName, value);
        }
    }

}
