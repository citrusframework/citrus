package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class JmsMessageReceiverParser extends AbstractJmsTemplateAwareParser {

    @Override
    protected BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.jms.JmsMessageReceiver");
        
        String replyTimeout = element.getAttribute(JmsParserConstants.RECEIVE_TIMEOUT_ATTRIBUTE);
        
        if (StringUtils.hasText(replyTimeout)) {
            builder.addPropertyValue(JmsParserConstants.RECEIVE_TIMEOUT_PROPERTY, replyTimeout);
        }
        
        return builder;
    }

}
