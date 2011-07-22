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
import org.w3c.dom.Element;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;

/**
 * Bean definition parser for sync-message-channel-sender configuration.
 * 
 * @author Christoph Deppisch
 */
public class SyncMessageChannelSenderParser extends AbstractMessageChannelTemplateAwareParser {

    /**
     * @see com.consol.citrus.config.xml.AbstractMessageChannelTemplateAwareParser#doParseComponent(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.channel.SyncMessageChannelSender");

        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("channel"), "channel");

        BeanDefinitionParserUtils.setPropertyValue(builder, 
                element.getAttribute("channel-name"), "channelName");

        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("channel-resolver"), "channelResolver");

        BeanDefinitionParserUtils.setPropertyValue(builder, 
                element.getAttribute("reply-timeout"), "replyTimeout");

        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("reply-handler"), "replyMessageHandler");
        
        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("reply-message-correlator"), "correlator");
        
        return builder;
    }

}
