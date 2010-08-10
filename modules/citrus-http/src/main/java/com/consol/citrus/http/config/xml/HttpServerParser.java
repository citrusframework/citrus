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
 * Parser for Http server implementation in Citrus http namespace.
 * 
 * @author Christoph Deppisch
 */
public class HttpServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.http.HttpServer");
        
        
        String port = element.getAttribute(HttpParserConstants.PORT_ATTRIBUTE);
        
        if (StringUtils.hasText(port)) {
            builder.addPropertyValue(HttpParserConstants.PORT_PROPERTY, port);
        }
        
        String uri = element.getAttribute(HttpParserConstants.URI_ATTRIBUTE);
        
        if (StringUtils.hasText(uri)) {
            builder.addPropertyValue(HttpParserConstants.URI_PROPERTY, uri);
        }
        
        String deamon = element.getAttribute(HttpParserConstants.DEAMON_ATTRIBUTE);
        
        if (StringUtils.hasText(deamon)) {
            builder.addPropertyValue(HttpParserConstants.DEAMON_PROPERTY, deamon);
        }
        
        String autoStart = element.getAttribute(HttpParserConstants.AUTOSTART_ATTRIBUTE);
        
        if (StringUtils.hasText(autoStart)) {
            builder.addPropertyValue(HttpParserConstants.AUTOSTART_PROPERTY, autoStart);
        }
        
        String messageHandler = element.getAttribute(HttpParserConstants.MESSAGE_HANDLER_ATTRIBUTE);
        
        if (StringUtils.hasText(messageHandler)) {
            builder.addPropertyReference(HttpParserConstants.MESSAGE_HANDLER_PROPERTY, messageHandler);
        }
        
        return builder.getBeanDefinition();
    }
}
