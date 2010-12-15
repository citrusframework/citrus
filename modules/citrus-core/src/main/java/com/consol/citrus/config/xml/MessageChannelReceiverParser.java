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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for message-channel-receiver configuration.
 * 
 * @author Christoph Deppisch
 */
public class MessageChannelReceiverParser extends AbstractMessageChannelTemplateAwareParser {

    /**
     * @see com.consol.citrus.config.xml.AbstractMessageChannelTemplateAwareParser#doParseComponent(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = getBeanDefinitionBuilder(element, parserContext);
        
        String channel = element.getAttribute("channel");
        
        if (StringUtils.hasText(channel)) {
            builder.addPropertyReference("channel", channel);
        }
        
        String channelName = element.getAttribute("channel-name");
        
        if (StringUtils.hasText(channelName)) {
            builder.addPropertyValue("channelName", channelName);
        }
        
        String channelResolver = element.getAttribute("channel-resolver");
        
        if (StringUtils.hasText(channelResolver)) {
            builder.addPropertyReference("channelResolver", channelResolver);
        }
        
        String receiveTimeout = element.getAttribute("receive-timeout");
        
        if (StringUtils.hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", receiveTimeout);
        }
        
        return builder;
    }
    
    /**
     * Get the bean definition builder. Subclasses may add some logic here.
     * @param element the actual xml element.
     * @param parserContext the current parser context.
     * @return the bean definition builder.
     */
    protected BeanDefinitionBuilder getBeanDefinitionBuilder(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(
                "com.consol.citrus.channel.MessageChannelReceiver");
    }
}
