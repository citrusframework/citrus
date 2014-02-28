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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;

/**
 * Bean definition parser for jms-sync-message-sender configuration.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsSyncMessageSenderParser extends AbstractJmsConfigParser {

    /**
     * @see com.consol.citrus.config.xml.AbstractJmsConfigParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.jms.JmsSyncMessageSender");
        
        String replyDestination = element.getAttribute("reply-destination");
        String replyDestinationName = element.getAttribute("reply-destination-name");
        
        if (StringUtils.hasText(replyDestination)) {
            builder.addPropertyReference("replyDestination", replyDestination);
        } else if (StringUtils.hasText(replyDestinationName)){
            builder.addPropertyValue("replyDestinationName", replyDestinationName);
        }
        
        BeanDefinitionParserUtils.setPropertyValue(builder, 
                element.getAttribute("reply-timeout"), "replyTimeout");

        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("reply-handler"), "replyMessageHandler");
        
        BeanDefinitionParserUtils.setPropertyReference(builder, 
                element.getAttribute("reply-message-correlator"), "correlator");
        
        return builder;
    }

}
