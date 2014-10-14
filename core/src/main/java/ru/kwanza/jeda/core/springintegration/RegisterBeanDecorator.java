package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

/**
 * @author: Guzanov Alexander
 */
class RegisterBeanDecorator implements BeanDefinitionDecorator {
    public BeanDefinitionHolder decorate(Node node,
                                         BeanDefinitionHolder beanDefinitionHolder,
                                         ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(FakeObjectHolder.class);
        builder.addConstructorArgReference(IJedaManager.class.getName());
        builder.addConstructorArgValue(beanDefinitionHolder.getBeanName());
        builder.addConstructorArgReference(beanDefinitionHolder.getBeanName());
        AbstractBeanDefinition fakeBeanDefinition = builder.getBeanDefinition();
        parserContext.getRegistry().registerBeanDefinition(
                parserContext.getReaderContext().generateBeanName(fakeBeanDefinition), fakeBeanDefinition);
        return beanDefinitionHolder;
    }
}
