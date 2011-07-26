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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;

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
        
        String destination = element.getAttribute("destination");
        String destinationName = element.getAttribute("destination-name");
        
        if (StringUtils.hasText(destination) || StringUtils.hasText(destinationName)) {
            //connectionFactory
            String connectionFactory = "connectionFactory"; //default value
            
            if (element.hasAttribute("connection-factory")) {
                connectionFactory = element.getAttribute("connection-factory");
            }
            
            if (!StringUtils.hasText(connectionFactory)) {
                parserContext.getReaderContext().error("Attribute connection-factory must not be empty " +
                		"for jms configuration elements", element);
            }
            
            builder.addPropertyReference("connectionFactory", connectionFactory);
            
            //destination
            if (StringUtils.hasText(destination)) {
                builder.addPropertyReference("destination", destination);
            } else {
                builder.addPropertyValue("destinationName", destinationName);
            }
        } else if (!StringUtils.hasText(element.getAttribute("jms-template"))){
            throw new BeanCreationException("Either a jms-template reference " +
                    "or one of destination or destination-name must be provided.");
        }
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("pub-sub-domain"), "pubSubDomain");
        
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
