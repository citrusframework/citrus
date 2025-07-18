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
import org.citrusframework.endpoint.Endpoint;
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
    OpenApiSpecificationSourceAwareBuilder<TestAction> {

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

    @Override
    public OpenApiSpecificationSource getOpenApiSpecificationSource() {
        return openApiSpecificationSource;
    }

    /**
     * Receive Http requests as server.
     */
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

    /**
     * Sends Http response messages as server. Uses default Http status 200 OK.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey) {
        return send(operationKey, HttpStatus.OK);
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, HttpStatus status) {
        return send(operationKey, String.valueOf(status.value()));
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, HttpStatus status,
        String accept) {
        return send(operationKey, String.valueOf(status.value()), accept);
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, String statusCode) {
        return send(operationKey, statusCode, null);
    }

    /**
     * Send Http response messages as server to client.
     */
    public OpenApiServerResponseActionBuilder send(String operationKey, String statusCode,
        String accept) {
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

    /**
     * Sets the Spring bean application context.
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
}
