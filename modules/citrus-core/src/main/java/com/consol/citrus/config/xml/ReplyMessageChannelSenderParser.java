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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for jms-reply-message-sender configuration.
 * 
 * @author Christoph Deppisch
 */
public class ReplyMessageChannelSenderParser extends AbstractMessageChannelTemplateAwareParser {

    /**
     * @see com.consol.citrus.config.xml.AbstractMessageChannelTemplateAwareParser#doParseComponent(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected BeanDefinitionBuilder doParseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.channel.ReplyMessageChannelSender");
        
        String destinationHolder = element.getAttribute("reply-channel-holder");
        
        if (StringUtils.hasText(destinationHolder)) {
            builder.addPropertyReference("replyChannelHolder", destinationHolder);
        } else {
            throw new BeanCreationException("Element must define 'reply-channel-holder' attribute");
        }
        
        String replyMessageCorrelator = element.getAttribute("reply-message-corelator");
        
        if (StringUtils.hasText(replyMessageCorrelator)) {
            builder.addPropertyReference("correlator", replyMessageCorrelator);
        }
        
        return builder;
    }

   
}
