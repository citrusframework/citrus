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
