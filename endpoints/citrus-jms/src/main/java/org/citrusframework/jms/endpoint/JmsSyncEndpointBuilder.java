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

package org.citrusframework.jms.endpoint;

import jakarta.jms.Destination;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-jms")
@XmlType(name = "", propOrder = {})
public class JmsSyncEndpointBuilder extends AbstractJmsEndpointBuilder<JmsSyncEndpoint, JmsSyncEndpointBuilder> {

    private final JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

    private String correlator;

    @Override
    public JmsSyncEndpoint build() {
        if (referenceResolver != null && StringUtils.hasText(correlator)) {
            correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
        }

        return super.build();
    }

    @Override
    protected JmsSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the reply destinationName property.
     */
    public JmsSyncEndpointBuilder replyDestination(String destinationName) {
        endpoint.getEndpointConfiguration().setReplyDestinationName(destinationName);
        return this;
    }

    @SchemaProperty(description = "Sets the reply destination name.")
    @XmlAttribute(name = "reply-destination")
    public void setReplyDestination(String replyDestination) {
        replyDestination(replyDestination);
    }

    /**
     * Sets the reply destination property.
     */
    public JmsSyncEndpointBuilder replyDestination(Destination destination) {
        endpoint.getEndpointConfiguration().setReplyDestination(destination);
        return this;
    }

    /**
     * Sets the polling interval.
     */
    public JmsSyncEndpointBuilder pollingInterval(int pollingInterval) {
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
    public JmsSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }
}
