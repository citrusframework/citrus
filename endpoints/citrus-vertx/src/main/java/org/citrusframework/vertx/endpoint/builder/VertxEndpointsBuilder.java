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

package org.citrusframework.vertx.endpoint.builder;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.vertx.endpoint.AbstractVertxEndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxEndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxSyncEndpointBuilder;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-vertx")
@XmlType(name = "", propOrder = {
        "asynchronous",
        "synchronous"
})
@XmlRootElement(name = "vertx")
public class VertxEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private AbstractVertxEndpointBuilder<?, ?> delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String host;
    private Integer port;
    private String address;
    private String vertxFactory;
    private String messageConverter;
    private Boolean pubSubDomain;
    private Integer pollingInterval;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous Vert.x endpoint.")
    @XmlElement
    public void setSynchronous(VertxSyncEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the Vert.x endpoint.")
    @XmlElement
    public void setAsynchronous(VertxEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "The Vert.x event bus host.")
    @XmlAttribute
    public void setHost(String host) {
        this.host = host;
    }

    @SchemaProperty(description = "The Vert.x event bus port.")
    @XmlAttribute
    public void setPort(int port) {
        this.port = port;
    }

    @SchemaProperty(description = "The event bus address.")
    @XmlAttribute
    public void setAddress(String address) {
        this.address = address;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom Vert.x factory.")
    @XmlAttribute(name = "vertx-factory")
    public void setVertxFactory(String vertxFactory) {
        this.vertxFactory = vertxFactory;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses publish/subscribe mode.")
    @XmlAttribute(name = "pub-sub-domain")
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new VertxEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (host != null) {
            delegate.setHost(host);
        }
        if (port != null) {
            delegate.setPort(port);
        }
        if (address != null) {
            delegate.setAddress(address);
        }
        if (vertxFactory != null) {
            delegate.setVertxFactory(vertxFactory);
        }
        if (messageConverter != null) {
            delegate.setMessageConverter(messageConverter);
        }
        if (pubSubDomain != null) {
            delegate.setPubSubDomain(pubSubDomain);
        }
        if (pollingInterval != null) {
            delegate.setPollingInterval(pollingInterval);
        }
        if (timeout != null) {
            delegate.setTimeout(timeout);
        }

        if (referenceResolver != null && delegate instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return delegate != null && delegate.supports(endpointType);
    }

    @Override
    @XmlTransient
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
