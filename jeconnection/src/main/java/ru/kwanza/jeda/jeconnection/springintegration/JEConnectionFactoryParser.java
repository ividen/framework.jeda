package ru.kwanza.jeda.jeconnection.springintegration;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinitionParser;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Guzanov Alexander
 */
class JEConnectionFactoryParser extends JedaBeanDefinitionParser {

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder definitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(JEConnectionFactory.class);

        definitionBuilder.addConstructorArgReference(IJedaManager.class.getName());
        definitionBuilder.addPropertyValue("path", element.getAttribute("path"));
        definitionBuilder.setDestroyMethodName("destroy");

        String envConfig = element.getAttribute("envConfig");
        if (StringUtils.hasText(envConfig)) {
            definitionBuilder.addPropertyReference("environmentConfig", envConfig);
        }

        String txConfig = element.getAttribute("txConfig");
        if (StringUtils.hasText(txConfig)) {
            definitionBuilder.addPropertyReference("transactionConfig", txConfig);
        }

        String lockingTimeout = element.getAttribute("lockingTimeout");
        if (StringUtils.hasText(lockingTimeout)) {
            definitionBuilder.addPropertyValue("lockingTimeout", lockingTimeout);
        }

        return createJedaDefinition(definitionBuilder.getBeanDefinition(), JEConnectionFactory.class, element, parserContext);
    }
}
