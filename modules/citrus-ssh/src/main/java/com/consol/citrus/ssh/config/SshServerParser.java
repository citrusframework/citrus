/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh.config;

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
public class SshServerParser extends AbstractBeanDefinitionParser {

    final static String ATTRIBUTE_PROPERTY_MAPPING[] = {
            "port","port",
            "auto-start","autoStart",
            "host-key-path","hostKeyPath",
            "user","user",
            "password","password",
            "allowed-key-path","allowedKeyPath",
    };

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition("com.consol.citrus.ssh.CitrusSshServer");

        for (int i = 0;i < ATTRIBUTE_PROPERTY_MAPPING.length; i+=2) {
            String value = element.getAttribute(ATTRIBUTE_PROPERTY_MAPPING[i]);
            if (StringUtils.hasText(value)) {
                builder.addPropertyValue(ATTRIBUTE_PROPERTY_MAPPING[i+1], value);
            }
        }

        // Setup message handler

        // TODO: Allow an inner bean for specifying the message handler
        String handlerRef = element.getAttribute("message-handler-ref");
        if (StringUtils.hasText(handlerRef)) {
            builder.addPropertyReference("messageHandler",handlerRef);
        }

        return builder.getBeanDefinition();
    }
}
