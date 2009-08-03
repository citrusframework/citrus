package com.consol.citrus.http.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class HttpMessageSenderParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.http.message.HttpMessageSender");
        
        String requestUrl = element.getAttribute(HttpParserConstants.REQUEST_URL_ATTRIBUTE);
        
        if (StringUtils.hasText(requestUrl)) {
            builder.addPropertyReference(HttpParserConstants.REQUEST_URL_PROPERTY, requestUrl);
        }
    
        String requestMethod = element.getAttribute(HttpParserConstants.REQUEST_METHOD_ATTRIBUTE);
        
        if (StringUtils.hasText(requestMethod)) {
            builder.addPropertyReference(HttpParserConstants.REQUEST_METHOD_PROPERTY, requestMethod);
        }
        
        String replyHandler = element.getAttribute(HttpParserConstants.REPLY_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(replyHandler)) {
            builder.addPropertyReference(HttpParserConstants.REPLY_HANDLER_PROPERTY, replyHandler);
        }
    
        return builder.getBeanDefinition();
    }
}
