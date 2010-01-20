/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

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
