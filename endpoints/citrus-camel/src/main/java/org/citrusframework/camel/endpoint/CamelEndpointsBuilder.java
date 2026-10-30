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

package org.citrusframework.camel.endpoint;

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
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-camel")
@XmlType(name = "", propOrder = {
        "asynchronous",
        "synchronous"
})
@XmlRootElement(name = "camel")
public class CamelEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private AbstractCamelEndpointBuilder<?, ?> delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String endpointUri;
    private String camelContext;
    private String messageConverter;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous Camel endpoint.")
    @XmlElement
    public void setSynchronous(CamelSyncEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the Camel endpoint.")
    @XmlElement
    public void setAsynchronous(CamelEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "The Camel endpoint uri.")
    @XmlAttribute(name = "endpoint-uri")
    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }

    @SchemaProperty(description = "The Camel context to use.")
    @XmlAttribute(name = "camel-context")
    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new CamelEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (endpointUri != null) {
            delegate.setEndpointUri(endpointUri);
        }
        if (camelContext != null) {
            delegate.setCamelContext(camelContext);
        }
        if (messageConverter != null) {
            delegate.setMessageConverter(messageConverter);
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
