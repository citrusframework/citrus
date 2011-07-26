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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.message.MessageSender.ErrorHandlingStrategy;

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
        
        String restTemplate = element.getAttribute("rest-template");
        if (StringUtils.hasText(restTemplate)){
            if (element.hasAttribute("request-factory")) {
                throw new BeanCreationException("When providing a 'rest-template' property, " +
                		"no 'request-factory' should be set!");
            }
            
            builder.addConstructorArgReference(restTemplate);
        } else {
            BeanDefinitionParserUtils.addConstructorArgReference(builder, element.getAttribute("request-factory"));
        }
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("request-url"), "requestUrl");
    
        String requestMethod = element.getAttribute("request-method");
        if (StringUtils.hasText(requestMethod)) {
            builder.addPropertyValue("requestMethod", new TypedStringValue(requestMethod, HttpMethod.class));
        }
        
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("reply-handler"), "replyMessageHandler");

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("reply-message-correlator"), "correlator");
        
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("endpoint-resolver"), "endpointUriResolver");
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("charset"), "charset");
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("content-type"), "contentType");
        
        if (element.hasAttribute("error-strategy")) {
            builder.addPropertyValue("errorHandlingStrategy", 
                    ErrorHandlingStrategy.fromName(element.getAttribute("error-strategy")));
        }
        
        return builder.getBeanDefinition();
    }
}
