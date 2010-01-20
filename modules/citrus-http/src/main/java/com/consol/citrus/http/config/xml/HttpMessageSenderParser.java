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

package com.consol.citrus.http.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class HttpMessageSenderParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.http.message.HttpMessageSender");
        
        String requestUrl = element.getAttribute(HttpParserConstants.REQUEST_URL_ATTRIBUTE);
        
        if (StringUtils.hasText(requestUrl)) {
            builder.addPropertyValue(HttpParserConstants.REQUEST_URL_PROPERTY, requestUrl);
        }
    
        String requestMethod = element.getAttribute(HttpParserConstants.REQUEST_METHOD_ATTRIBUTE);
        
        if (StringUtils.hasText(requestMethod)) {
            builder.addPropertyValue(HttpParserConstants.REQUEST_METHOD_PROPERTY, requestMethod);
        }
        
        String replyHandler = element.getAttribute(HttpParserConstants.REPLY_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(replyHandler)) {
            builder.addPropertyReference(HttpParserConstants.REPLY_HANDLER_PROPERTY, replyHandler);
        }

        String replyMessageCorrelator = element.getAttribute(HttpParserConstants.REPLY_CORRELATOR_ATTRIBUTE);
        
        if (StringUtils.hasText(replyMessageCorrelator)) {
            builder.addPropertyReference(HttpParserConstants.REPLY_CORRELATOR_PROPERTY, replyMessageCorrelator);
        }
        
        return builder.getBeanDefinition();
    }
}
