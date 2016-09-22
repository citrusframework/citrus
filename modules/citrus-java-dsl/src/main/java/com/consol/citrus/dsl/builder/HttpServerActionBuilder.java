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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * Action executes http server operations such as receiving requests and sending response messages.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

    /** Spring application context */
    private ApplicationContext applicationContext;

    /** Target http client instance */
    private final Endpoint httpServer;

    /**
     * Default constructor.
     */
    public HttpServerActionBuilder(DelegatingTestAction<TestAction> action, Endpoint httpServer) {
        super(action);
        this.httpServer = httpServer;
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
        HttpServerRequestActionBuilder httpServerRequestActionBuilder = new HttpServerRequestActionBuilder(action, httpServer)
                .withApplicationContext(applicationContext)
                .method(method);

        if (StringUtils.hasText(path)) {
            httpServerRequestActionBuilder.path(path);
        }

        return httpServerRequestActionBuilder;
    }

    /**
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public HttpServerActionBuilder withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
            return new HttpServerResponseActionBuilder(action, httpServer)
                    .withApplicationContext(applicationContext);
        }

        /**
         * Generic response builder for sending response messages to client with response status code.
         * @return
         */
        public HttpServerResponseActionBuilder response(HttpStatus status) {
            return new HttpServerResponseActionBuilder(action, httpServer)
                    .withApplicationContext(applicationContext)
                    .status(status);
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
}
