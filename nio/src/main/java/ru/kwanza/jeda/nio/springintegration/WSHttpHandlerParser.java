package ru.kwanza.jeda.nio.springintegration;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import ru.kwanza.jeda.nio.server.http.JedaHttpHandler;
import ru.kwanza.jeda.nio.server.http.JedaWSHttpHandler;

/**
 * @author Alexander Guzanov
 */
class WSHttpHandlerParser extends HttpHandlerParser {

    @Override
    protected void parseParameters(Element element, BeanDefinitionBuilder definitionBuilder) {
        super.parseParameters(element, definitionBuilder);
        String wsdl = element.getAttribute("wsdl");

        if (StringUtils.hasText(wsdl)) {
            definitionBuilder.addConstructorArgValue(wsdl);
        }
    }

    @Override
    protected Class<JedaWSHttpHandler> getHandlerClass() {
        return JedaWSHttpHandler.class;
    }
}
