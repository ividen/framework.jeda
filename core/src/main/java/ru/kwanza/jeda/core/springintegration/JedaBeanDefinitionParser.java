package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
public abstract class JedaBeanDefinitionParser extends AbstractBeanDefinitionParser {
    protected JedaBeanDefinition createFlexFlowDefinition(AbstractBeanDefinition originalDefinition,
                                                              Class type,
                                                              Element e,
                                                              ParserContext parserContext) {
        String id = generateId(e, parserContext, originalDefinition);

        return createFlexFlowDefinition(id, originalDefinition, type, e, parserContext);
    }


    protected JedaBeanDefinition createFlexFlowDefinition(String name,
                                                              AbstractBeanDefinition originalDefinition,
                                                              Class type,
                                                              Element e,
                                                              ParserContext parserContext) {

        return new JedaBeanDefinition(name, type, originalDefinition);
    }

    protected String generateId(Element element, ParserContext parserContext, AbstractBeanDefinition rawBeanDefinition) {
        return super.resolveId(element, rawBeanDefinition, parserContext);
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        if (definition instanceof JedaBeanDefinition) {
            return ((JedaBeanDefinition) definition).getId();
        }

        return generateId(element, parserContext, definition);
    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }
}

