package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class JmsReplyMessageSenderParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.jms.JmsReplyMessageSender");
        
        String jmsTemplate = element.getAttribute(JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE);
        
        if (StringUtils.hasText(jmsTemplate)) {
            if (element.hasAttribute(JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE)) {
                throw new BeanCreationException("When providing a '" + JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE + "' reference, no " +
                        "'" + JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE + "' should be provided.");
            }
            builder.addPropertyReference(JmsParserConstants.JMS_TEMPLATE_PROPERTY, jmsTemplate);
        } else {
            //connectionFactory
            String connectionFactory = "connectionFactory"; //default value
            
            if(element.hasAttribute(JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE)) {
                connectionFactory = element.getAttribute(JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE);
            }
            
            if(!StringUtils.hasText(connectionFactory)) {
                parserContext.getReaderContext().error(
                        "'" + JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE + "' attribute must not be empty for jms configuration elements", element);
            }
            
            builder.addPropertyReference(JmsParserConstants.CONNECTION_FACTORY_PROPERTY, connectionFactory);
        }
        
        String destinationHolder = element.getAttribute(JmsParserConstants.DESTINATION_HOLDER_ATTRIBUTE);
        
        if (StringUtils.hasText(destinationHolder)) {
            builder.addPropertyReference(JmsParserConstants.DESTINATION_HOLDER_PROPERTY, destinationHolder);
        } else {
            
        }
        
        return builder.getBeanDefinition();
    }

   
}
