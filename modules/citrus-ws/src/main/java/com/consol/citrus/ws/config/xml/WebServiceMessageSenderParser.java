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

package com.consol.citrus.ws.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for ws message sender in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class WebServiceMessageSenderParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.ws.message.WebServiceMessageSender");
        
        String requestUrl = element.getAttribute(WSParserConstants.REQUEST_URL_ATTRIBUTE);
        
        if (StringUtils.hasText(requestUrl)) {
            builder.addPropertyValue(WSParserConstants.REQUEST_URL_PROPERTY, requestUrl);
        }
    
        String webServiceTemplate = element.getAttribute(WSParserConstants.WS_TEMPLATE_ATTRIBUTE);
        
        if(StringUtils.hasText(webServiceTemplate)) {
        	if (element.hasAttribute(WSParserConstants.MESSAGE_FACTORY_ATTRIBUTE) ||
                    element.hasAttribute(WSParserConstants.MESSAGE_SENDER_ATTRIBUTE) ||
                    element.hasAttribute(WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE)) {
                throw new BeanCreationException("When providing a '" + WSParserConstants.WS_TEMPLATE_ATTRIBUTE + "' reference, none of " +
                        "'" + WSParserConstants.MESSAGE_FACTORY_ATTRIBUTE + "', '" + WSParserConstants.MESSAGE_SENDER_ATTRIBUTE + 
                        "', or '" + WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE + "' should be provided.");
            }
        	
        	builder.addPropertyReference(WSParserConstants.WS_TEMPLATE_PROPERTY, webServiceTemplate);
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
        
        String messageSender = element.getAttribute(WSParserConstants.MESSAGE_SENDER_ATTRIBUTE);
		String messageSenderList = element.getAttribute(WSParserConstants.MESSAGE_SENDERS_ATTRIBUTE);
		if (StringUtils.hasText(messageSender) && StringUtils.hasText(messageSenderList)) {
			parserContext.getReaderContext().error("Either 'message-sender' or 'message-senders' attribute has to be specified - not both of them.", element);
		}
		
		if (StringUtils.hasText(messageSender)) {
			builder.addPropertyReference(WSParserConstants.MESSAGE_SENDER_PROPERTY, messageSender);
		} else if (StringUtils.hasText(messageSenderList)) {
			builder.addPropertyReference(WSParserConstants.MESSAGE_SENDERS_PROPERTY, messageSenderList);
		}
		
        String replyHandler = element.getAttribute(WSParserConstants.REPLY_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(replyHandler)) {
            builder.addPropertyReference(WSParserConstants.REPLY_HANDLER_PROPERTY, replyHandler);
        }
        
        String replyMessageCorrelator = element.getAttribute(WSParserConstants.REPLY_CORRELATOR_ATTRIBUTE);
        
        if (StringUtils.hasText(replyMessageCorrelator)) {
            builder.addPropertyReference(WSParserConstants.REPLY_CORRELATOR_PROPERTY, replyMessageCorrelator);
        }

        return builder.getBeanDefinition();
    }
}
