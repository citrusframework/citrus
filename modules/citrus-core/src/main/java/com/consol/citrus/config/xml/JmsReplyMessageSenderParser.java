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
 * Bean definition parser for jms-reply-message-sender configuration.
 * 
 * @author Christoph Deppisch
 */
public class JmsReplyMessageSenderParser extends AbstractBeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#parseInternal(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
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
            throw new BeanCreationException("Element must define " + JmsParserConstants.DESTINATION_HOLDER_ATTRIBUTE + " attribute");
        }
        
        String replyMessageCorrelator = element.getAttribute(JmsParserConstants.REPLY_CORRELATOR_ATTRIBUTE);
        
        if (StringUtils.hasText(replyMessageCorrelator)) {
            builder.addPropertyReference(JmsParserConstants.REPLY_CORRELATOR_PROPERTY, replyMessageCorrelator);
        }
        
        return builder.getBeanDefinition();
    }

   
}
