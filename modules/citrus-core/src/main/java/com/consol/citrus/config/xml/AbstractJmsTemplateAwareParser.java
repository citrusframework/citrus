package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public abstract class AbstractJmsTemplateAwareParser extends AbstractJmsConfigParser {

    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = doParseComponent(element, parserContext);
        
        String jmsTemplate = element.getAttribute(JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE);
        
        if (StringUtils.hasText(jmsTemplate)) {
            if (element.hasAttribute(JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE) ||
                    element.hasAttribute(JmsParserConstants.DESTINATION_ATTRIBUTE) ||
                    element.hasAttribute(JmsParserConstants.DESTINATION_NAME_ATTRIBUTE)) {
                throw new BeanCreationException("When providing a '" + JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE + "' reference, none of " +
                        "'" + JmsParserConstants.CONNECTION_FACTORY_ATTRIBUTE + "', '" + JmsParserConstants.DESTINATION_ATTRIBUTE + 
                        "', or '" + JmsParserConstants.DESTINATION_NAME_ATTRIBUTE + "' should be provided.");
            }
            builder.addPropertyReference(JmsParserConstants.JMS_TEMPLATE_PROPERTY, jmsTemplate);
        }
        
        return builder;
    }
    
    protected abstract BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext);
}
