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

package org.citrusframework.http.actions;

import org.citrusframework.TestAction;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

/**
 * Action executes http client operations such as sending requests and receiving responses.
 *
 * @since 2.4
 */
public class HttpClientActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.actions.http.HttpClientActionBuilder<TestAction, HttpClientActionBuilder> {

    /** Target http client instance */
    private Endpoint httpClient;
    private String httpClientUri;

    /**
     * Default constructor.
     */
    public HttpClientActionBuilder() {
    }

    /**
     * Default constructor.
     */
    public HttpClientActionBuilder(Endpoint httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Default constructor.
     */
    public HttpClientActionBuilder(String httpClientUri) {
        this.httpClientUri = httpClientUri;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpClientSendActionBuilder send() {
        return new HttpClientSendActionBuilder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpClientReceiveActionBuilder receive() {
        return new HttpClientReceiveActionBuilder();
    }

    /**
     * Generic request builder with request method and path.
     */
    public HttpClientRequestActionBuilder request(HttpMethod method, String path) {
        return request(method.name(), path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpClientRequestActionBuilder request(String method, String path) {
        HttpClientRequestActionBuilder builder = new HttpClientRequestActionBuilder();
        if (httpClient != null) {
            builder.endpoint(httpClient);
        } else {
            builder.endpoint(httpClientUri);
        }

        builder.name("http:send-request");
        builder.withReferenceResolver(referenceResolver);
        builder.method(method);

        if (StringUtils.hasText(path)) {
            builder.path(path);
        }

        this.delegate = builder;
        return builder;
    }

    @Override
    public HttpClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    /**
     * Provides send request action methods.
     */
    public class HttpClientSendActionBuilder implements
            org.citrusframework.actions.http.HttpClientSendActionBuilder<SendMessageAction, HttpClientRequestActionBuilder.HttpMessageBuilderSupport, HttpClientRequestActionBuilder> {

        @Override
        public HttpClientRequestActionBuilder get() {
            return request(HttpMethod.GET, null);
        }

        @Override
        public HttpClientRequestActionBuilder get(String path) {
            return request(HttpMethod.GET, path);
        }

        @Override
        public HttpClientRequestActionBuilder post() {
            return request(HttpMethod.POST, null);
        }

        @Override
        public HttpClientRequestActionBuilder post(String path) {
            return request(HttpMethod.POST, path);
        }

        @Override
        public HttpClientRequestActionBuilder put() {
            return request(HttpMethod.PUT, null);
        }

        @Override
        public HttpClientRequestActionBuilder put(String path) {
            return request(HttpMethod.PUT, path);
        }

        @Override
        public HttpClientRequestActionBuilder delete() {
            return request(HttpMethod.DELETE, null);
        }

        @Override
        public HttpClientRequestActionBuilder delete(String path) {
            return request(HttpMethod.DELETE, path);
        }

        @Override
        public HttpClientRequestActionBuilder head() {
            return request(HttpMethod.HEAD, null);
        }

        @Override
        public HttpClientRequestActionBuilder head(String path) {
            return request(HttpMethod.HEAD, path);
        }

        @Override
        public HttpClientRequestActionBuilder options() {
            return request(HttpMethod.OPTIONS, null);
        }

        @Override
        public HttpClientRequestActionBuilder options(String path) {
            return request(HttpMethod.OPTIONS, path);
        }

        @Override
        public HttpClientRequestActionBuilder trace() {
            return request(HttpMethod.TRACE, null);
        }

        @Override
        public HttpClientRequestActionBuilder trace(String path) {
            return request(HttpMethod.TRACE, path);
        }

        @Override
        public HttpClientRequestActionBuilder patch() {
            return request(HttpMethod.PATCH, null);
        }

        @Override
        public HttpClientRequestActionBuilder patch(String path) {
            return request(HttpMethod.PATCH, path);
        }
    }

    /**
     * Provides receive response action methods.
     */
    public class HttpClientReceiveActionBuilder implements
            org.citrusframework.actions.http.HttpClientReceiveActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder.HttpMessageBuilderSupport, HttpClientResponseActionBuilder> {

        @Override
        public HttpClientResponseActionBuilder response() {
            HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
            if (httpClient != null) {
                builder.endpoint(httpClient);
            } else {
                builder.endpoint(httpClientUri);
            }

            builder.name("http:receive-response");
            builder.withReferenceResolver(referenceResolver);
            HttpClientActionBuilder.this.delegate = builder;
            return builder;
        }

        /**
         * Generic response builder for expecting response messages on client with response status code.
         */
        public HttpClientResponseActionBuilder response(HttpStatusCode status) {
            return response(status.value());
        }

        @Override
        public HttpClientResponseActionBuilder response(Object status) {
            if (status instanceof HttpStatusCode statusCode) {
                return response(statusCode);
            } else {
                throw new CitrusRuntimeException("Invalid status code type: " + status.getClass().getName());
            }
        }

        @Override
        public HttpClientResponseActionBuilder response(int status) {
            HttpClientResponseActionBuilder builder = new HttpClientResponseActionBuilder();
            if (httpClient != null) {
                builder.endpoint(httpClient);
            } else {
                builder.endpoint(httpClientUri);
            }

            builder.name("http:receive-response");
            builder.withReferenceResolver(referenceResolver);
            builder.message().status(status);
            HttpClientActionBuilder.this.delegate = builder;
            return builder;
        }
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
