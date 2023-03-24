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

package org.citrusframework.config.handler;

import org.citrusframework.config.xml.ChannelEndpointAdapterParser;
import org.citrusframework.config.xml.ChannelEndpointParser;
import org.citrusframework.config.xml.ChannelSyncEndpointParser;
import org.citrusframework.config.xml.MessageSelectingQueueChannelParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for components in Citrus configuration.
 *
 * @author Christoph Deppisch
 */
public class CitrusSpringIntegrationConfigNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("channel", new MessageSelectingQueueChannelParser());
        registerBeanDefinitionParser("message-channel", new MessageSelectingQueueChannelParser());
        registerBeanDefinitionParser("channel-endpoint", new ChannelEndpointParser());
        registerBeanDefinitionParser("channel-sync-endpoint", new ChannelSyncEndpointParser());
        registerBeanDefinitionParser("channel-endpoint-adapter", new ChannelEndpointAdapterParser());
    }

}
