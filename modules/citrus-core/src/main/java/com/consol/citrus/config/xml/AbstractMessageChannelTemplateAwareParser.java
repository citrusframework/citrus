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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Abstract bean definition parser is aware of message channel attribute.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageChannelTemplateAwareParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = doParseComponent(element, parserContext);
        
        String msgChannelTemplate = element.getAttribute("message-channel-template");
        
        if (StringUtils.hasText(msgChannelTemplate)) {
            builder.addPropertyReference("messageChannelTemplate", msgChannelTemplate);
        }
        
        return builder.getBeanDefinition();
    }
    
    /**
     * Subclasses must implement this method in order to provide
     * detailed bean definition parsing.
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext);
}
