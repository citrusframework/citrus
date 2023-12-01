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

package org.citrusframework.http.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * Action executes http server operations such as receiving requests and sending response messages.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(INDENT_OUTPUT);

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target http client instance */
    private Endpoint httpServer;
    private String httpServerUri;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public HttpServerActionBuilder(Endpoint httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * Default constructor.
     */
    public HttpServerActionBuilder(String httpServerUri) {
        this.httpServerUri = httpServerUri;
    }

    /**
     * Generic response builder for sending response messages to client.
     * @return
     */
    public HttpServerResponseActionBuilder respond() {
        return new HttpServerSendActionBuilder().response();
    }

    /**
     * Generic response builder for sending response messages to client with response status code.
     * @return
     */
    public HttpServerResponseActionBuilder respond(HttpStatus status) {
        return new HttpServerSendActionBuilder().response(status);
    }

    /**
     * Generic response builder for sending JSON response messages to client with response status 200 (OK).
     *
     * @return
     */
    public HttpServerResponseActionBuilder.HttpMessageBuilderSupport respondOkJson(String json) {
        return new HttpServerSendActionBuilder()
                .response(HttpStatus.OK)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(json);
    }

    /**
     * Generic response builder for sending JSON response messages to client with response status 200 (OK).
     *
     * @return
     */
    public HttpServerResponseActionBuilder.HttpMessageBuilderSupport respondOkJson(Object json) {
        try {
            return respondOkJson(OBJECT_MAPPER.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to write JSON body as string!", e);
        }
    }

    /**
     * Receive Http requests as server.
     */
    public HttpServerReceiveActionBuilder receive() {
        return new HttpServerReceiveActionBuilder();
    }

    /**
     * Send Http response messages as server to client.
     */
    public HttpServerSendActionBuilder send() {
        return new HttpServerSendActionBuilder();
    }

    /**
     * Generic request builder with request method and path.
     * @param method
     * @param path
     * @return
     */
    private HttpServerRequestActionBuilder request(HttpMethod method, String path) {
        HttpServerRequestActionBuilder builder = new HttpServerRequestActionBuilder();
        if (httpServer != null) {
            builder.endpoint(httpServer);
        } else {
            builder.endpoint(httpServerUri);
        }

        builder.name("http:receive-request");
        builder.withReferenceResolver(referenceResolver);
        builder.message().method(method);

        if (StringUtils.hasText(path)) {
            builder.path(path);
        }

        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the Spring bean application context.
     * @param referenceResolver
     */
    public HttpServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    /**
     * Provides send response action methods.
     */
    public class HttpServerSendActionBuilder {
        /**
         * Generic response builder for sending response messages to client.
         * @return
         */
        public HttpServerResponseActionBuilder response() {
            HttpServerResponseActionBuilder builder =  new HttpServerResponseActionBuilder();
            if (httpServer != null) {
                builder.endpoint(httpServer);
            } else {
                builder.endpoint(httpServerUri);
            }

            builder.name("http:send-response");
            builder.withReferenceResolver(referenceResolver);

            HttpServerActionBuilder.this.delegate = builder;
            return builder;
        }

        /**
         * Generic response builder for sending response messages to client with response status code.
         * @return
         */
        public HttpServerResponseActionBuilder response(HttpStatus status) {
            HttpServerResponseActionBuilder builder = new HttpServerResponseActionBuilder();
            if (httpServer != null) {
                builder.endpoint(httpServer);
            } else {
                builder.endpoint(httpServerUri);
            }

            builder.name("http:send-response");
            builder.withReferenceResolver(referenceResolver);
            builder.message().status(status);

            HttpServerActionBuilder.this.delegate = builder;
            return builder;
        }
    }

    /**
     * Provides receive request action methods.
     */
    public class HttpServerReceiveActionBuilder {
        /**
         * Receive Http GET request as server.
         */
        public HttpServerRequestActionBuilder get() {
            return request(HttpMethod.GET, null);
        }

        /**
         * Receive Http GET request as server.
         */
        public HttpServerRequestActionBuilder get(String path) {
            return request(HttpMethod.GET, path);
        }

        /**
         * Receive Http POST request as server.
         */
        public HttpServerRequestActionBuilder post() {
            return request(HttpMethod.POST, null);
        }

        /**
         * Receive Http POST request as server.
         */
        public HttpServerRequestActionBuilder post(String path) {
            return request(HttpMethod.POST, path);
        }

        /**
         * Receive Http PUT request as server.
         */
        public HttpServerRequestActionBuilder put() {
            return request(HttpMethod.PUT, null);
        }

        /**
         * Receive Http PUT request as server.
         */
        public HttpServerRequestActionBuilder put(String path) {
            return request(HttpMethod.PUT, path);
        }

        /**
         * Receive Http DELETE request as server.
         */
        public HttpServerRequestActionBuilder delete() {
            return request(HttpMethod.DELETE, null);
        }

        /**
         * Receive Http DELETE request as server.
         */
        public HttpServerRequestActionBuilder delete(String path) {
            return request(HttpMethod.DELETE, path);
        }

        /**
         * Receive Http HEAD request as server.
         */
        public HttpServerRequestActionBuilder head() {
            return request(HttpMethod.HEAD, null);
        }

        /**
         * Receive Http HEAD request as server.
         */
        public HttpServerRequestActionBuilder head(String path) {
            return request(HttpMethod.HEAD, path);
        }

        /**
         * Receive Http OPTIONS request as server.
         */
        public HttpServerRequestActionBuilder options() {
            return request(HttpMethod.OPTIONS, null);
        }

        /**
         * Receive Http OPTIONS request as server.
         */
        public HttpServerRequestActionBuilder options(String path) {
            return request(HttpMethod.OPTIONS, path);
        }

        /**
         * Receive Http TRACE request as server.
         */
        public HttpServerRequestActionBuilder trace() {
            return request(HttpMethod.TRACE, null);
        }

        /**
         * Receive Http TRACE request as server.
         */
        public HttpServerRequestActionBuilder trace(String path) {
            return request(HttpMethod.TRACE, path);
        }

        /**
         * Receive Http PATCH request as server.
         */
        public HttpServerRequestActionBuilder patch() {
            return request(HttpMethod.PATCH, null);
        }

        /**
         * Receive Http PATCH request as server.
         */
        public HttpServerRequestActionBuilder patch(String path) {
            return request(HttpMethod.PATCH, path);
        }
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
