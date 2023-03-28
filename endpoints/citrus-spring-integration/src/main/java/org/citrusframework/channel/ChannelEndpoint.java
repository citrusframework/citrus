/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.channel;

import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Basic message endpoint sends and receives message from Spring message channel. When receiving messages channel must
 * implement {@link org.springframework.messaging.PollableChannel} interface. When using message selector channel
 * must be of type {@link org.citrusframework.channel.MessageSelectingQueueChannel}.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelEndpoint extends AbstractEndpoint implements BeanFactoryAware {

    /** Cached producer or consumer */
    private ChannelConsumer channelConsumer;
    private ChannelProducer channelProducer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public ChannelEndpoint() {
        super(new ChannelEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    public ChannelEndpoint(ChannelEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (channelConsumer == null) {
            channelConsumer = new ChannelConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return channelConsumer;
    }

    @Override
    public Producer createProducer() {
        if (channelProducer == null) {
            channelProducer = new ChannelProducer(getProducerName(), getEndpointConfiguration());
        }

        return channelProducer;
    }

    @Override
    public ChannelEndpointConfiguration getEndpointConfiguration() {
        return (ChannelEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        getEndpointConfiguration().setBeanFactory(beanFactory);
    }

}
