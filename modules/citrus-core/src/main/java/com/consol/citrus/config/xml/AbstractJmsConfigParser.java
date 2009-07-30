package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;


public abstract class AbstractJmsConfigParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = doParse(element, parserContext);
        
        String destination = element.getAttribute(JmsParserConstants.DESTINATION_ATTRIBUTE);
        String destinationName = element.getAttribute(JmsParserConstants.DESTINATION_NAME_ATTRIBUTE);
        
        if(StringUtils.hasText(destination) || StringUtils.hasText(destinationName)) {
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
            
            //destination
            if (StringUtils.hasText(destination)) {
                builder.addPropertyReference(JmsParserConstants.DESTINATION_PROPERTY, destination);
            } else {
                builder.addPropertyValue(JmsParserConstants.DESTINATION_NAME_PROPERTY, destinationName);
            }
        } else {
            throw new BeanCreationException("Either a '" + JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE + "' reference " +
                    "or one of '" + JmsParserConstants.DESTINATION_ATTRIBUTE + "' or '" + JmsParserConstants.DESTINATION_NAME_ATTRIBUTE + "' must be provided.");
        }
        
        return builder.getBeanDefinition();
    }
    
    protected abstract BeanDefinitionBuilder doParse(Element element, ParserContext parserContext);
}
