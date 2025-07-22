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

package org.citrusframework.actions;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

public interface PurgeMessageChannelActionBuilder<T extends TestAction>
        extends ActionBuilder<T, PurgeMessageChannelActionBuilder<T>>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, PurgeMessageChannelActionBuilder<T>> {

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    PurgeMessageChannelActionBuilder<T> selector(MessageSelector messageSelector);

    /**
     * Sets the bean reference resolver channel resolver for using channel names.
     */
    PurgeMessageChannelActionBuilder<T> channelResolver(ReferenceResolver referenceResolver);

    /**
     * Sets the channelResolver for using channel names.
     * @param channelResolver the channelResolver to set
     */
    PurgeMessageChannelActionBuilder<T> channelResolver(DestinationResolver<MessageChannel> channelResolver);

    /**
     * Adds list of channel names to purge in this action.
     * @param channelNames the channelNames to set
     */
    PurgeMessageChannelActionBuilder<T> channelNames(List<String> channelNames);

    /**
     * Adds several channel names to the list of channels to purge in this action.
     */
    PurgeMessageChannelActionBuilder<T> channelNames(String... channelNames);

    /**
     * Adds a channel name to the list of channels to purge in this action.
     */
    PurgeMessageChannelActionBuilder<T> channel(String name);

    /**
     * Adds list of channels to purge in this action.
     * @param channels the channels to set
     */
    PurgeMessageChannelAction.Builder channels(List<MessageChannel> channels);

    /**
     * Sets several channels to purge in this action.
     */
    PurgeMessageChannelAction.Builder channels(MessageChannel... channels);

    /**
     * Adds a channel to the list of channels to purge in this action.
     */
    PurgeMessageChannelAction.Builder channel(MessageChannel channel);

    /**
     * Sets the Spring bean factory for using endpoint names.
     */
    PurgeMessageChannelAction.Builder withApplicationContext(ApplicationContext applicationContext);

    PurgeMessageChannelAction.Builder beanFactory(BeanFactory beanFactory);

    interface BuilderFactory {

        PurgeMessageChannelActionBuilder<?> purgeChannels();

    }

}
