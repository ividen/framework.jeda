package ru.kwanza.jeda.nio.springintegration;

import ru.kwanza.jeda.api.internal.ISystemManager;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.nio.server.http.FlexFlowHttpHandler;
import ru.kwanza.jeda.nio.server.http.IHttpHandler;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class HttpHandlerParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(FlexFlowHttpHandler.class);

        String stage = DomUtils.getChildElementValueByTagName(element, "stage");
        String flowBus = DomUtils.getChildElementValueByTagName(element, "flowBus");
        String timeout = element.getAttribute("timeout");
        String timedOutHandler = element.getAttribute("timedOutHandler");

        String constructorName = null;
        String paramReference = null;
        if (StringUtils.hasText(stage)) {
            paramReference = stage;
            constructorName = "createForStage";
        } else if (StringUtils.hasText(flowBus)) {
            paramReference = flowBus;
            constructorName = "createForFlowBus";
        }
        definitionBuilder.setFactoryMethod(constructorName);
        definitionBuilder.addConstructorArgReference(ISystemManager.class.getName());
        definitionBuilder.addConstructorArgReference(paramReference);

        if (StringUtils.hasText(timedOutHandler)) {
            definitionBuilder.addPropertyReference("timedOutHandler", timedOutHandler);
        }

        if (StringUtils.hasText(timeout)) {
            definitionBuilder.addConstructorArgValue(timeout);
        }

        JedaBeanDefinition result = createJedaDefinition(definitionBuilder.getBeanDefinition(), IHttpHandler.class, element, parserContext);

        String server = element.getAttribute("server");
        String uri = DomUtils.getChildElementValueByTagName(element, "uri");
        String pattern = DomUtils.getChildElementValueByTagName(element, "pattern");
        String registrationMethod;
        String registrationAddress;
        if (StringUtils.hasText(uri)) {
            registrationMethod = "registerHandlerByURI";
            registrationAddress = uri;
        } else {
            registrationMethod = "registerHandlerByPattern";
            registrationAddress = pattern;
        }


        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RegistrationFactory.class);
        builder.setFactoryMethod(registrationMethod);
        builder.addConstructorArgReference(server);
        builder.addConstructorArgValue(registrationAddress);
        builder.addConstructorArgReference(result.getId());
        AbstractBeanDefinition fakeBeanDefinition = builder.getBeanDefinition();
        parserContext.getRegistry().registerBeanDefinition(
                parserContext.getReaderContext().generateBeanName(fakeBeanDefinition), fakeBeanDefinition);


        return result;
    }
}
