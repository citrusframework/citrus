package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class JmsSyncMessageSenderParser extends AbstractJmsConfigParser {

    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.jms.JmsSyncMessageSender");
        
        String replyDestination = element.getAttribute(JmsParserConstants.REPLY_DESTINATION_ATTRIBUTE);
        String replyDestinationName = element.getAttribute(JmsParserConstants.REPLY_DESTINATION_NAME_ATTRIBUTE);
        
        if (StringUtils.hasText(replyDestination)) {
            builder.addPropertyReference(JmsParserConstants.REPLY_DESTINATION_PROPERTY, replyDestination);
        } else if(StringUtils.hasText(replyDestinationName)){
            builder.addPropertyValue(JmsParserConstants.REPLY_DESTINATION_NAME_PROPERTY, replyDestinationName);
        }
        
        String replyTimeout = element.getAttribute(JmsParserConstants.REPLY_TIMEOUT_ATTRIBUTE);
        
        if (StringUtils.hasText(replyTimeout)) {
            builder.addPropertyValue(JmsParserConstants.REPLY_TIMEOUT_PROPERTY, replyTimeout);
        }
        
        String replyHandler = element.getAttribute(JmsParserConstants.REPLY_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(replyHandler)) {
            builder.addPropertyReference(JmsParserConstants.REPLY_HANDLER_PROPERTY, replyHandler);
        }
        
        return builder;
    }

}
