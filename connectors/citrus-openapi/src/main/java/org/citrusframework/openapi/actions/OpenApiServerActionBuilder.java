/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.openapi.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;
import org.springframework.http.HttpStatus;

/**
 * Action executes http server operations such as receiving requests and sending response messages.
 *
 * @author Christoph Deppisch
 * @since 4.1
 */
public class OpenApiServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final OpenApiSpecification specification;

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target http client instance */
    private Endpoint httpServer;
    private String httpServerUri;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public OpenApiServerActionBuilder(Endpoint httpServer, OpenApiSpecification specification) {
        this.httpServer = httpServer;
        this.specification = specification;
    }

    /**
     * Default constructor.
     */
    public OpenApiServerActionBuilder(String httpServerUri, OpenApiSpecification specification) {
        this.httpServerUri = httpServerUri;
        this.specification = specification;
    }

    /**
     * Receive Http requests as server.
     */
    public OpenApiServerRequestActionBuilder receive(String operationId) {
        OpenApiServerRequestActionBuilder builder = new OpenApiServerRequestActionBuilder(specification, operationId);
        if (httpServer != null) {
            builder.endpoint(httpServer);
        } else {
            builder.endpoint(httpServerUri);
        }

        builder.name("openapi:receive-request");
        builder.withReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    /**
     * Sends Http response messages as server.
     * Uses default Http status 200 OK.
     */
    public OpenApiServerResponseActionBuilder send(String operationId) {
        return send(operationId, HttpStatus.OK);
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationId, HttpStatus status) {
        return send(operationId, String.valueOf(status.value()));
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationId, String statusCode) {
        OpenApiServerResponseActionBuilder builder = new OpenApiServerResponseActionBuilder(specification, operationId, statusCode);
        if (httpServer != null) {
            builder.endpoint(httpServer);
        } else {
            builder.endpoint(httpServerUri);
        }

        builder.name("openapi:send-response");
        builder.withReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the Spring bean application context.
     * @param referenceResolver
     */
    public OpenApiServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     * @param referenceResolver
     */
    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        if (referenceResolver == null) {
            this.referenceResolver = referenceResolver;

            if (delegate instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) delegate).setReferenceResolver(referenceResolver);
            }
        }
    }
}
