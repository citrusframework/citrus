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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

/**
 * Action executes http server operations such as receiving requests and sending response messages.
 *
 * @since 2.4
 */
public class HttpServerActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.actions.http.HttpServerActionBuilder<TestAction, HttpServerActionBuilder> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(INDENT_OUTPUT);

    /** Target http client instance */
    private Endpoint httpServer;
    private String httpServerUri;

    /**
     * Default constructor.
     */
    public HttpServerActionBuilder() {
    }

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

    @Override
    public HttpServerResponseActionBuilder respond() {
        return new HttpServerSendActionBuilder().response();
    }

    /**
     * Generic response builder for sending response messages to client with response status code.
     */
    public HttpServerResponseActionBuilder respond(HttpStatus status) {
        return respond(status.value());
    }

    @Override
    public HttpServerResponseActionBuilder respond(int status) {
        return new HttpServerSendActionBuilder().response(status);
    }

    @Override
    public HttpServerResponseActionBuilder respond(Object status) {
        if (status instanceof HttpStatusCode statusCode) {
            return new HttpServerSendActionBuilder().response(statusCode);
        } else {
            throw new CitrusRuntimeException("Invalid status code type: " + status.getClass().getName());
        }
    }

    @Override
    public HttpServerResponseActionBuilder.HttpMessageBuilderSupport respondOkJson(String json) {
        return new HttpServerSendActionBuilder()
                .response(HttpStatus.OK.value())
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(json);
    }

    @Override
    public HttpServerResponseActionBuilder.HttpMessageBuilderSupport respondOkJson(Object json) {
        try {
            return respondOkJson(OBJECT_MAPPER.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to write JSON body as string!", e);
        }
    }

    @Override
    public HttpServerReceiveActionBuilder receive() {
        return new HttpServerReceiveActionBuilder();
    }

    @Override
    public HttpServerSendActionBuilder send() {
        return new HttpServerSendActionBuilder();
    }

    /**
     * Generic request builder with request method and path.
     */
    public HttpServerRequestActionBuilder request(HttpMethod method, String path) {
        return request(method.name(), path);
    }

    @Override
    public HttpServerRequestActionBuilder request(String method, String path) {
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

    @Override
    public HttpServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    /**
     * Provides send response action methods.
     */
    public class HttpServerSendActionBuilder implements
            org.citrusframework.actions.http.HttpServerSendActionBuilder<SendMessageAction, HttpServerResponseActionBuilder.HttpMessageBuilderSupport, HttpServerResponseActionBuilder> {

        @Override
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
         */
        public HttpServerResponseActionBuilder response(HttpStatusCode status) {
            return response(status.value());
        }

        @Override
        public HttpServerResponseActionBuilder response(Object status) {
            if (status instanceof HttpStatusCode statusCode) {
                return response(statusCode);
            } else {
                throw new CitrusRuntimeException("Invalid status code type: " + status.getClass().getName());
            }
        }

        @Override
        public HttpServerResponseActionBuilder response(int status) {
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
    public class HttpServerReceiveActionBuilder implements
            org.citrusframework.actions.http.HttpServerReceiveActionBuilder<ReceiveMessageAction, HttpServerRequestActionBuilder.HttpMessageBuilderSupport, HttpServerRequestActionBuilder> {

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
