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

package com.consol.citrus.http.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for Http sender implementation in Citrus http namespace.
 * 
 * @author Christoph Deppisch
 */
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
