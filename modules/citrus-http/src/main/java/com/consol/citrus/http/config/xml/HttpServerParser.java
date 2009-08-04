package com.consol.citrus.http.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class HttpServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.http.HttpServer");
        
        
        String port = element.getAttribute(HttpParserConstants.PORT_ATTRIBUTE);
        
        if (StringUtils.hasText(port)) {
            builder.addPropertyValue(HttpParserConstants.PORT_PROPERTY, port);
        }
        
        String uri = element.getAttribute(HttpParserConstants.URI_ATTRIBUTE);
        
        if (StringUtils.hasText(uri)) {
            builder.addPropertyValue(HttpParserConstants.URI_PROPERTY, uri);
        }
        
        String deamon = element.getAttribute(HttpParserConstants.DEAMON_ATTRIBUTE);
        
        if (StringUtils.hasText(deamon)) {
            builder.addPropertyValue(HttpParserConstants.DEAMON_PROPERTY, deamon);
        }
        
        String autoStart = element.getAttribute(HttpParserConstants.AUTOSTART_ATTRIBUTE);
        
        if (StringUtils.hasText(autoStart)) {
            builder.addPropertyValue(HttpParserConstants.AUTOSTART_PROPERTY, autoStart);
        }
        
        String messageHandler = element.getAttribute(HttpParserConstants.MESSAGE_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(messageHandler)) {
            builder.addPropertyReference(HttpParserConstants.MESSAGE_HANDLER_PROPERTY, messageHandler);
        }
        
        return builder.getBeanDefinition();
    }
}
