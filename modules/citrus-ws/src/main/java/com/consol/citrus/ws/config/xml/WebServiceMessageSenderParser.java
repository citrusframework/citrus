package com.consol.citrus.ws.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class WebServiceMessageSenderParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.ws.message.WebServiceMessageSender");
        
        String requestUrl = element.getAttribute(WSParserConstants.REQUEST_URL_ATTRIBUTE);
        
        if (StringUtils.hasText(requestUrl)) {
            builder.addPropertyValue(WSParserConstants.REQUEST_URL_PROPERTY, requestUrl);
        }
    
        String messageFactory = "messageFactory"; //default value
        
        if(element.hasAttribute(WSParserConstants.MESSAGE_FACTORY_ATTRIBUTE)) {
            messageFactory = element.getAttribute(WSParserConstants.MESSAGE_FACTORY_ATTRIBUTE);
        }
        
        
        if(!StringUtils.hasText(messageFactory)) {
            parserContext.getReaderContext().error(
                    "'" + WSParserConstants.MESSAGE_FACTORY_ATTRIBUTE + "' attribute must not be empty for element", element);
        }
        
        builder.addPropertyReference(WSParserConstants.MESSAGE_FACTORY_PROPERTY, messageFactory);
        
        String replyHandler = element.getAttribute(WSParserConstants.REPLY_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(replyHandler)) {
            builder.addPropertyReference(WSParserConstants.REPLY_HANDLER_PROPERTY, replyHandler);
        }
    
        return builder.getBeanDefinition();
    }
}
