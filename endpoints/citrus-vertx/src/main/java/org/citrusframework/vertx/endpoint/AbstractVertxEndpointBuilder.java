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

package org.citrusframework.vertx.endpoint;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.citrusframework.vertx.message.VertxMessageConverter;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-vertx")
@XmlType(name = "", propOrder = {})
@SuppressWarnings("unchecked")
public abstract class AbstractVertxEndpointBuilder<T extends VertxEndpoint, B extends AbstractVertxEndpointBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private String vertxFactory;
    private String messageConverter;

    @Override
    public T build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(vertxFactory)) {
                vertxFactory(referenceResolver.resolve(vertxFactory, VertxInstanceFactory.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, VertxMessageConverter.class));
            }
        }

        return super.build();
    }

    /**
     * Sets the host property.
     */
    public B host(String host) {
        getEndpoint().getEndpointConfiguration().setHost(host);
        return (B) this;
    }

    @SchemaProperty(description = "The Vert.x event bus host.")
    @XmlAttribute
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public B port(int port) {
        getEndpoint().getEndpointConfiguration().setPort(port);
        return (B) this;
    }

    @SchemaProperty(description = "The Vert.x event bus port.")
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the address property.
     */
    public B address(String address) {
        getEndpoint().getEndpointConfiguration().setAddress(address);
        return (B) this;
    }

    @SchemaProperty(description = "The event bus address.")
    @XmlAttribute
    public void setAddress(String address) {
        address(address);
    }

    /**
     * Sets the vertxFactory property.
     */
    public B vertxFactory(VertxInstanceFactory vertxFactory) {
        getEndpoint().setVertxInstanceFactory(vertxFactory);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom Vert.x factory.")
    @XmlAttribute(name = "vertx-factory")
    public void setVertxFactory(String vertxFactory) {
        this.vertxFactory = vertxFactory;
    }

    /**
     * Sets the messageConverter property.
     */
    public B messageConverter(VertxMessageConverter messageConverter) {
        getEndpoint().getEndpointConfiguration().setMessageConverter(messageConverter);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the pubSubDomain property.
     */
    public B pubSubDomain(boolean pubSubDomain) {
        getEndpoint().getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses publish/subscribe mode.")
    @XmlAttribute(name = "pub-sub-domain")
    public void setPubSubDomain(boolean pubSubDomain) {
        pubSubDomain(pubSubDomain);
    }

    /**
     * Sets the polling interval.
     */
    public B pollingInterval(int pollingInterval) {
        getEndpoint().getEndpointConfiguration().setPollingInterval(pollingInterval);
        return (B) this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public B timeout(long timeout) {
        getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        return (B) this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
