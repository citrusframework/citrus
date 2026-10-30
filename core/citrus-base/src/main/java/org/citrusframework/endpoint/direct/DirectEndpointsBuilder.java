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

package org.citrusframework.endpoint.direct;

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
import org.citrusframework.api.yaml.SchemaProperty;

@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "direct")
public class DirectEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private AbstractDirectEndpointBuilder<?, ?> delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String queueName;
    private Boolean autoCreateQueue;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous direct endpoint.")
    @XmlElement
    public void setSynchronous(DirectSyncEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the direct endpoint.")
    @XmlElement
    public void setAsynchronous(DirectEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "The queue name.")
    @XmlAttribute
    public void setQueue(String queueName) {
        this.queueName = queueName;
    }

    @XmlAttribute(name = "queue-name")
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @SchemaProperty(description = "When set the queue is automatically created when it does not exist in bean registry.")
    @XmlAttribute(name = "auto-create-queue")
    public void setAutoCreateQueue(boolean autoCreate) {
        this.autoCreateQueue = autoCreate;
    }

    @SchemaProperty(description = "The timeout when receiving messages from the queue.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new DirectEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (queueName != null) {
            delegate.setQueue(queueName);
        }
        if (autoCreateQueue != null) {
            delegate.setAutoCreateQueue(autoCreateQueue);
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
