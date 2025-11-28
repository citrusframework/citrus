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

package org.citrusframework.openapi.actions;

import org.citrusframework.TestAction;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;
import org.springframework.http.HttpStatus;

/**
 * Action executes http server operations such as receiving requests and sending response messages.
 *
 * @since 4.1
 */
public class OpenApiServerActionBuilder extends
    AbstractReferenceResolverAwareTestActionBuilder<TestAction> implements
    OpenApiSpecificationSourceAwareBuilder<TestAction>, org.citrusframework.actions.openapi.OpenApiServerActionBuilder<TestAction, OpenApiServerActionBuilder> {

    private final OpenApiSpecificationSource openApiSpecificationSource;

    /**
     * Target http client instance
     */
    private Endpoint httpServer;
    private String httpServerUri;

    /**
     * Default constructor.
     */
    public OpenApiServerActionBuilder(Endpoint httpServer,
        OpenApiSpecificationSource specification) {
        this.httpServer = httpServer;
        this.openApiSpecificationSource = specification;
    }

    /**
     * Default constructor.
     */
    public OpenApiServerActionBuilder(String httpServerUri,
        OpenApiSpecificationSource specification) {
        this.httpServerUri = httpServerUri;
        this.openApiSpecificationSource = specification;
    }

    public OpenApiServerActionBuilder(OpenApiSpecificationSource openApiSpecificationSource) {
        this.openApiSpecificationSource = openApiSpecificationSource;
    }

    @Override
    public OpenApiSpecificationSource getOpenApiSpecificationSource() {
        return openApiSpecificationSource;
    }

    @Override
    public OpenApiServerActionBuilder server(String httpServer) {
        this.httpServerUri = httpServer;
        return this;
    }

    @Override
    public OpenApiServerActionBuilder server(Endpoint httpServer) {
        this.httpServer = httpServer;
        return this;
    }

    @Override
    public OpenApiServerRequestActionBuilder receive(String operationKey) {
        OpenApiServerRequestActionBuilder builder = new OpenApiServerRequestActionBuilder(
            openApiSpecificationSource, operationKey);
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

    @Override
    public OpenApiServerResponseActionBuilder send(String operationKey) {
        return send(operationKey, HttpStatus.OK);
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, HttpStatus status) {
        return send(operationKey, status.name());
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, HttpStatus status, String accept) {
        return send(operationKey, status.name(), accept);
    }

    @Override
    public OpenApiServerResponseActionBuilder send(String operationKey, Object status) {
        if (status instanceof HttpStatus statusCode) {
            return send(operationKey, statusCode);
        } else {
            throw new CitrusRuntimeException("Invalid status code type: " + status.getClass().getName());
        }
    }

    @Override
    public OpenApiServerResponseActionBuilder send(String operationKey, int statusCode) {
        return send(operationKey, String.valueOf(statusCode));
    }

    @Override
    public OpenApiServerResponseActionBuilder send(String operationKey, String statusCode) {
        return send(operationKey, statusCode, null);
    }

    @Override
    public OpenApiServerResponseActionBuilder send(String operationKey, String statusCode, String accept) {
        OpenApiServerResponseActionBuilder builder = new OpenApiServerResponseActionBuilder(
            openApiSpecificationSource, operationKey, statusCode, accept);
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

    @Override
    public OpenApiServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof SendActionBuilder<?, ?, ?> messageActionBuilder) {
            if (httpServer != null) {
                messageActionBuilder.endpoint(httpServer);
            } else if (httpServerUri != null) {
                messageActionBuilder.endpoint(httpServerUri);
            }
        }

        if (delegate instanceof ReceiveActionBuilder<?, ?, ?> messageActionBuilder) {
            if (httpServer != null) {
                messageActionBuilder.endpoint(httpServer);
            } else if (httpServerUri != null) {
                messageActionBuilder.endpoint(httpServerUri);
            }
        }

        return delegate.build();
    }
}
