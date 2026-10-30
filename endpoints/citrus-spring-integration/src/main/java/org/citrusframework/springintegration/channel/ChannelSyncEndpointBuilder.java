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

package org.citrusframework.springintegration.channel;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

/**
 * @since 2.7.6
 */
@SchemaType(module = "citrus-spring-integration")
@XmlType(name = "", propOrder = {})
public class ChannelSyncEndpointBuilder extends AbstractChannelEndpointBuilder<ChannelSyncEndpoint, ChannelSyncEndpointBuilder> {

    private final ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

    private String correlator;

    @Override
    public ChannelSyncEndpoint build() {
        if (referenceResolver != null && StringUtils.hasText(correlator)) {
            correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
        }

        return super.build();
    }

    @Override
    protected ChannelSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the polling interval.
     */
    public ChannelSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the polling interval.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the message correlator.
     */
    public ChannelSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }
}
