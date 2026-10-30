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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-jms")
@XmlType(name = "", propOrder = {})
public class JmsEndpointBuilder extends AbstractJmsEndpointBuilder<JmsEndpoint, JmsEndpointBuilder> {

    private final JmsEndpoint endpoint = new JmsEndpoint();

    @Override
    protected JmsEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the autoStart property.
     */
    public JmsEndpointBuilder autoStart(boolean autoStart) {
        endpoint.getEndpointConfiguration().setAutoStart(autoStart);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "When enabled the JMS consumer is started right after the endpoint is created.")
    @XmlAttribute(name = "auto-start")
    public void setAutoStart(boolean autoStart) {
        autoStart(autoStart);
    }

    /**
     * Sets the durableSubscription property.
     */
    public JmsEndpointBuilder durableSubscription(boolean durableSubscription) {
        endpoint.getEndpointConfiguration().setDurableSubscription(durableSubscription);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "Enables/disables durable subscription mode.")
    @XmlAttribute(name = "durable-subscription")
    public void setDurableSubscription(boolean durableSubscription) {
        durableSubscription(durableSubscription);
    }

    /**
     * Sets the durableSubscriberName property.
     */
    public JmsEndpointBuilder durableSubscriberName(String durableSubscriberName) {
        endpoint.getEndpointConfiguration().setDurableSubscriberName(durableSubscriberName);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "Sets the durable subscriber name.")
    @XmlAttribute(name = "durable-subscriber-name")
    public void setDurableSubscriberName(String durableSubscriberName) {
        durableSubscriberName(durableSubscriberName);
    }
}
