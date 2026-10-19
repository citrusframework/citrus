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
package org.citrusframework.grpc.builder;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.grpc.server.GrpcServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.jspecify.annotations.Nullable;

/**
 * Root builder for gRPC client and server endpoints, used by the XML and YAML endpoint builders.
 */
@SchemaType(oneOf = { "client", "server" }, module = "citrus-grpc")
@XmlType(name = "", propOrder = {
        "client",
        "server"
})
@XmlRootElement(name = "grpc")
public class GrpcEndpointBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private EndpointBuilder<?> delegate;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(description = "Sets the gRPC client endpoint.")
    @XmlElement
    public void setClient(GrpcClientBuilder client) {
        this.delegate = client;
    }

    @SchemaProperty(description = "Sets the gRPC server endpoint.")
    @XmlElement
    public void setServer(GrpcServerBuilder server) {
        this.delegate = server;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Client/server endpoint builder has not been initialized");
        }

        if (referenceResolver != null && delegate instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return delegate.supports(endpointType);
    }

    @Override
    @XmlTransient
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
