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

import static org.springframework.http.HttpStatus.OK;

/**
 * Action executes http client operations such as sending requests and receiving responses.
 *
 * @since 4.1
 */
public class OpenApiClientActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements OpenApiSpecificationSourceAwareBuilder<TestAction>, org.citrusframework.actions.openapi.OpenApiClientActionBuilder<TestAction, OpenApiClientActionBuilder> {

    private final OpenApiSpecificationSource openApiSpecificationSource;

    /**
     * Target http client instance
     */
    private Endpoint httpClient;
    private String httpClientUri;

    /**
     * Default constructor.
     */
    public OpenApiClientActionBuilder(Endpoint httpClient, OpenApiSpecificationSource openApiSpecificationSource) {
        this.httpClient = httpClient;
        this.openApiSpecificationSource = openApiSpecificationSource;
    }

    /**
     * Default constructor.
     */
    public OpenApiClientActionBuilder(String httpClientUri, OpenApiSpecificationSource openApiSpecificationSource) {
        this.httpClientUri = httpClientUri;
        this.openApiSpecificationSource = openApiSpecificationSource;
    }

    public OpenApiClientActionBuilder(OpenApiSpecificationSource openApiSpecificationSource) {
        this.openApiSpecificationSource = openApiSpecificationSource;
    }

    @Override
    public OpenApiSpecificationSource getOpenApiSpecificationSource() {
        return openApiSpecificationSource;
    }

    @Override
    public OpenApiClientActionBuilder client(String httpClient) {
        this.httpClientUri = httpClient;
        getOpenApiSpecificationSource().setHttpClient(httpClient);
        return this;
    }

    @Override
    public OpenApiClientActionBuilder client(Endpoint httpClient) {
        this.httpClient = httpClient;
        getOpenApiSpecificationSource().setHttpClient(httpClient);
        return this;
    }

    @Override
    public OpenApiClientRequestActionBuilder send(String operationKey) {
        OpenApiClientRequestActionBuilder builder = new OpenApiClientRequestActionBuilder(openApiSpecificationSource, operationKey);
        if (httpClient != null) {
            builder.endpoint(httpClient);
        } else {
            builder.endpoint(httpClientUri);
        }

        builder.name("openapi:send-request");
        builder.withReferenceResolver(referenceResolver);

        this.delegate = builder;
        return builder;
    }

    @Override
    public OpenApiClientResponseActionBuilder receive(String operationKey) {
        return receive(operationKey, OK);
    }

    /**
     * Receives Http response messages as client with given status.
     */
    public OpenApiClientResponseActionBuilder receive(String operationKey, HttpStatus status) {
        return receive(operationKey, status.name());
    }

    @Override
    public OpenApiClientResponseActionBuilder receive(String operationKey, Object status) {
        if (status instanceof HttpStatus statusCode) {
            return receive(operationKey, statusCode);
        } else {
            throw new CitrusRuntimeException("Invalid status code type: " + status.getClass().getName());
        }
    }

    @Override
    public OpenApiClientResponseActionBuilder receive(String operationKey, int statusCode) {
        return receive(operationKey, String.valueOf(statusCode));
    }

    @Override
    public OpenApiClientResponseActionBuilder receive(String operationKey, String statusCode) {
        OpenApiClientResponseActionBuilder builder = new OpenApiClientResponseActionBuilder(openApiSpecificationSource, operationKey, statusCode);
        if (httpClient != null) {
            builder.endpoint(httpClient);
        } else {
            builder.endpoint(httpClientUri);
        }

        builder.name("openapi:receive-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     */
    public OpenApiClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof SendActionBuilder<?, ?, ?> messageActionBuilder) {
            if (httpClient != null) {
                messageActionBuilder.endpoint(httpClient);
            } else if (httpClientUri != null) {
                messageActionBuilder.endpoint(httpClientUri);
            }
        }

        if (delegate instanceof ReceiveActionBuilder<?, ?, ?> messageActionBuilder) {
            if (httpClient != null) {
                messageActionBuilder.endpoint(httpClient);
            } else if (httpClientUri != null) {
                messageActionBuilder.endpoint(httpClientUri);
            }
        }

        return delegate.build();
    }
}
