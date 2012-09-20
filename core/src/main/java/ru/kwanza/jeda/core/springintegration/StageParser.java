package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IStage;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author Guzanov Alexander
 */
class StageParser extends FlexFlowBeanDefinitionParser {
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SystemStageFactory.class);
        List<Element> childElements = DomUtils.getChildElements(element);
        XmlReaderContext readerContext = parserContext.getReaderContext();
        NamespaceHandlerResolver namespaceHandlerResolver = readerContext.getNamespaceHandlerResolver();
        String name = element.getAttribute("name");


        StageBeanBuilder beanBuilder = new StageBeanBuilder(element.getAttribute("transaction"));
        for (Element e : childElements) {
            String namespaceURI = e.getNamespaceURI();
            NamespaceHandler handler = namespaceHandlerResolver.resolve(namespaceURI);
            if (handler == null) {
                readerContext.error("Unable to locate Spring NamespaceHandler" +
                        " for XML schema namespace [" + namespaceURI + "]", e);
            } else {
                BeanDefinition bean = handler.parse(e, parserContext);
                if (bean instanceof FlexFlowBeanDefinition) {
                    beanBuilder.addBean((FlexFlowBeanDefinition) bean);
                }
            }
        }
        beanBuilder.build(definitionBuilder);
        return createFlexFlowDefinition(name, definitionBuilder.getBeanDefinition(), IStage.class,
                element, parserContext);
    }
}
