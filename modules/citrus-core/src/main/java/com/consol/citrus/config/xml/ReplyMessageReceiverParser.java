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

import com.consol.citrus.message.ReplyMessageReceiver;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;

/**
 * Generic bean definition parser for all reply-message-handler configurations. This parser is referenced
 * by several reply message receiver configuration components (e.g. jms, http, message-channel, ssh)
 * 
 * @author Christoph Deppisch
 */
public class ReplyMessageReceiverParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ReplyMessageReceiver.class);
        
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
        
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("polling-interval"), "pollingInterval");
        
        return builder.getBeanDefinition();
    }
   
}
