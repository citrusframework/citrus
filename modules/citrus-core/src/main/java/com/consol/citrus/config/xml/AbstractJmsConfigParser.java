/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Abstract base class for JMS related configuration. Sender and receiver bean definitions use
 * this base parser to configure attributes like connection factory or JMS template.
 *  
 * @author Christoph Deppisch
 */
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
        } else if(!StringUtils.hasText(element.getAttribute(JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE))){
            throw new BeanCreationException("Either a '" + JmsParserConstants.JMS_TEMPLATE_ATTRIBUTE + "' reference " +
                    "or one of '" + JmsParserConstants.DESTINATION_ATTRIBUTE + "' or '" + JmsParserConstants.DESTINATION_NAME_ATTRIBUTE + "' must be provided.");
        }
        
        String pubSubDomain = element.getAttribute(JmsParserConstants.PUB_SUB_DOMAIN_ATTRIBUTE);
        if(StringUtils.hasText(pubSubDomain)) {
            builder.addPropertyValue(JmsParserConstants.PUB_SUB_DOMAIN_PROPERTY, pubSubDomain);
        }
        
        return builder.getBeanDefinition();
    }
    
    /**
     * Subclasses must implement this parsing method in order to
     * provide detailed bean definition building.
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract BeanDefinitionBuilder doParse(Element element, ParserContext parserContext);
}
