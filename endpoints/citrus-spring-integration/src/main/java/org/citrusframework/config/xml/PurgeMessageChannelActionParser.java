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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.actions.PurgeMessageChannelAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for purge-channel action in test case.
 *
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelActionParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeMessageChannelActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        if (element.hasAttribute("message-selector")) {
            BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("message-selector"), "messageSelector");
        }

        List<String> channelNames = new ArrayList<String>();
        ManagedList<BeanDefinition> channelRefs = new ManagedList<BeanDefinition>();
        List<Element> channelElements = DomUtils.getChildElementsByTagName(element, "channel");
        for (Element channel : channelElements) {
            String channelName = channel.getAttribute("name");
            String channelRef = channel.getAttribute("ref");

            if (StringUtils.hasText(channelName)) {
                channelNames.add(channelName);
            } else if (StringUtils.hasText(channelRef)) {
                channelRefs.add(BeanDefinitionBuilder.childBeanDefinition(channelRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'channel' must set one of the attributes 'name' or 'ref'");
            }
        }

        beanDefinition.addPropertyValue("channelNames", channelNames);
        beanDefinition.addPropertyValue("channels", channelRefs);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class PurgeMessageChannelActionFactoryBean extends AbstractTestActionFactoryBean<PurgeMessageChannelAction, PurgeMessageChannelAction.Builder> implements BeanFactoryAware {

        private final PurgeMessageChannelAction.Builder builder = new PurgeMessageChannelAction.Builder();

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            builder.beanFactory(beanFactory);
        }

        /**
         * Sets the channelNames.
         * @param channelNames the channelNames to set
         */
        public void setChannelNames(List<String> channelNames) {
            builder.channelNames(channelNames);
        }

        /**
         * Sets the channels.
         * @param channels the channels to set
         */
        public void setChannels(List<MessageChannel> channels) {
            builder.channels(channels);
        }

        /**
         * Sets the messageSelector.
         * @param messageSelector the messageSelector to set
         */
        public void setMessageSelector(MessageSelector messageSelector) {
            builder.selector(messageSelector);
        }

        /**
         * Sets the channelResolver.
         * @param channelResolver the channelResolver to set
         */
        public void setChannelResolver(DestinationResolver<MessageChannel> channelResolver) {
            builder.channelResolver(channelResolver);
        }

        @Override
        public PurgeMessageChannelAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return PurgeMessageChannelAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public PurgeMessageChannelAction.Builder getBuilder() {
            return builder;
        }
    }
}
