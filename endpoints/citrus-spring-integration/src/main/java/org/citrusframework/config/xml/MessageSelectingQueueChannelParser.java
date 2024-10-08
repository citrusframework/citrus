/*
 * Copyright the original author or authors.
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

package org.citrusframework.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import org.citrusframework.channel.MessageSelectingQueueChannel;
import org.citrusframework.config.util.BeanDefinitionParserUtils;

/**
 * Bean definition parser for special message channel configuration which
 * supports message selection.
 *
 */
public class MessageSelectingQueueChannelParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MessageSelectingQueueChannel.class);

        BeanDefinitionParserUtils.setConstructorArgValue(builder, element.getAttribute("capacity"));

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("logging"), "loggingEnabled");

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("polling-interval"), "pollingInterval");

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }
}
