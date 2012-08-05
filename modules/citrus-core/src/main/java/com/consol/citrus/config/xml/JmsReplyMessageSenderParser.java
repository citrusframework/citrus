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
 * Bean definition parser for jms-reply-message-sender configuration.
 * 
 * @author Christoph Deppisch
 */
public class JmsReplyMessageSenderParser extends AbstractBeanDefinitionParser {

    /** Connection factory attribute */
    private static final String CONNECTION_FACTORY_ATTRIBUTE = "connection-factory";
    
    /**
     * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#parseInternal(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.jms.JmsReplyMessageSender");
        
        String jmsTemplate = element.getAttribute("jms-template");
        
        if (StringUtils.hasText(jmsTemplate)) {
            if (element.hasAttribute(CONNECTION_FACTORY_ATTRIBUTE)) {
                throw new BeanCreationException("When providing a jms-template reference, no " +
                        CONNECTION_FACTORY_ATTRIBUTE + " should be provided.");
            }
            builder.addPropertyReference("jmsTemplate", jmsTemplate);
        } else {
            //connectionFactory
            String connectionFactory = "connectionFactory"; //default value
            
            if (element.hasAttribute(CONNECTION_FACTORY_ATTRIBUTE)) {
                connectionFactory = element.getAttribute(CONNECTION_FACTORY_ATTRIBUTE);
            }
            
            if (!StringUtils.hasText(connectionFactory)) {
                parserContext.getReaderContext().error(
                        "connection-factory attribute must not be empty for jms configuration elements", element);
            }
            
            builder.addPropertyReference("connectionFactory", connectionFactory);
        }
        
        String destinationHolder = element.getAttribute("reply-destination-holder");
        
        if (StringUtils.hasText(destinationHolder)) {
            builder.addPropertyReference("replyDestinationHolder", destinationHolder);
        } else {
            throw new BeanCreationException("Element must define reply-destination-holder attribute");
        }
        
        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("reply-message-correlator"), "correlator");
        
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
        
        return builder.getBeanDefinition();
    }

   
}
