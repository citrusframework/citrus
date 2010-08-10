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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for jetty-server component in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class JettyServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.ws.JettyServer");
        
        
        String port = element.getAttribute(WSParserConstants.PORT_ATTRIBUTE);
        
        if (StringUtils.hasText(port)) {
            builder.addPropertyValue(WSParserConstants.PORT_PROPERTY, port);
        }
        
        String autoStart = element.getAttribute(WSParserConstants.AUTOSTART_ATTRIBUTE);
        
        if (StringUtils.hasText(autoStart)) {
            builder.addPropertyValue(WSParserConstants.AUTOSTART_PROPERTY, autoStart);
        }
        
        String resourceBase = element.getAttribute(WSParserConstants.RESOURCE_BASE_ATTRIBUTE);
        
        if (StringUtils.hasText(resourceBase)) {
            builder.addPropertyValue(WSParserConstants.RESOURCE_BASE_PROPERTY, resourceBase);
        }
        
        String contextConfigLocation = element.getAttribute(WSParserConstants.CONTEXT_CONFIC_LOCATION_ATTRIBUTE);
        
        if (StringUtils.hasText(contextConfigLocation)) {
            builder.addPropertyValue(WSParserConstants.CONTEXT_CONFIC_LOCATION_PROPERTY, contextConfigLocation);
        }
        
        String useRootContext = element.getAttribute(WSParserConstants.USE_ROOT_CONTEXT_ATTRIBUTE);
        
        if (StringUtils.hasText(useRootContext)) {
            builder.addPropertyValue(WSParserConstants.USE_ROOT_CONTEXT_PROPERTY, Boolean.valueOf(useRootContext));
        }
        
        return builder.getBeanDefinition();
    }
}
