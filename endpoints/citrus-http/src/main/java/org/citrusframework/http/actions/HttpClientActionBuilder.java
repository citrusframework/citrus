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
import org.citrusframework.endpoint.Endpoint;
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
public class HttpClientActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction> {

    /** Target http client instance */
    private Endpoint httpClient;
    private String httpClientUri;

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

    /**
     * Sends Http requests as client.
     */
    public HttpClientSendActionBuilder send() {
        return new HttpClientSendActionBuilder();
    }

    /**
     * Receives Http response messages as client.
     */
    public HttpClientReceiveActionBuilder receive() {
        return new HttpClientReceiveActionBuilder();
    }

    /**
     * Generic request builder with request method and path.
     * @param method
     * @param path
     * @return
     */
    private HttpClientRequestActionBuilder request(HttpMethod method, String path) {
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

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public HttpClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    /**
     * Provides send request action methods.
     */
    public class HttpClientSendActionBuilder {

        /**
         * Sends Http GET request as client to server.
         */
        public HttpClientRequestActionBuilder get() {
            return request(HttpMethod.GET, null);
        }

        /**
         * Sends Http GET request as client to server.
         */
        public HttpClientRequestActionBuilder get(String path) {
            return request(HttpMethod.GET, path);
        }

        /**
         * Sends Http POST request as client to server.
         */
        public HttpClientRequestActionBuilder post() {
            return request(HttpMethod.POST, null);
        }

        /**
         * Sends Http POST request as client to server.
         */
        public HttpClientRequestActionBuilder post(String path) {
            return request(HttpMethod.POST, path);
        }

        /**
         * Sends Http PUT request as client to server.
         */
        public HttpClientRequestActionBuilder put() {
            return request(HttpMethod.PUT, null);
        }

        /**
         * Sends Http PUT request as client to server.
         */
        public HttpClientRequestActionBuilder put(String path) {
            return request(HttpMethod.PUT, path);
        }

        /**
         * Sends Http DELETE request as client to server.
         */
        public HttpClientRequestActionBuilder delete() {
            return request(HttpMethod.DELETE, null);
        }

        /**
         * Sends Http DELETE request as client to server.
         */
        public HttpClientRequestActionBuilder delete(String path) {
            return request(HttpMethod.DELETE, path);
        }

        /**
         * Sends Http HEAD request as client to server.
         */
        public HttpClientRequestActionBuilder head() {
            return request(HttpMethod.HEAD, null);
        }

        /**
         * Sends Http HEAD request as client to server.
         */
        public HttpClientRequestActionBuilder head(String path) {
            return request(HttpMethod.HEAD, path);
        }

        /**
         * Sends Http OPTIONS request as client to server.
         */
        public HttpClientRequestActionBuilder options() {
            return request(HttpMethod.OPTIONS, null);
        }

        /**
         * Sends Http OPTIONS request as client to server.
         */
        public HttpClientRequestActionBuilder options(String path) {
            return request(HttpMethod.OPTIONS, path);
        }

        /**
         * Sends Http TRACE request as client to server.
         */
        public HttpClientRequestActionBuilder trace() {
            return request(HttpMethod.TRACE, null);
        }

        /**
         * Sends Http TRACE request as client to server.
         */
        public HttpClientRequestActionBuilder trace(String path) {
            return request(HttpMethod.TRACE, path);
        }

        /**
         * Sends Http PATCH request as client to server.
         */
        public HttpClientRequestActionBuilder patch() {
            return request(HttpMethod.PATCH, null);
        }

        /**
         * Sends Http PATCH request as client to server.
         */
        public HttpClientRequestActionBuilder patch(String path) {
            return request(HttpMethod.PATCH, path);
        }
    }

    /**
     * Provides receive response action methods.
     */
    public class HttpClientReceiveActionBuilder {
        /**
         * Generic response builder for expecting response messages on client.
         * @return
         */
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
         * @return
         */
        public HttpClientResponseActionBuilder response(HttpStatusCode status) {
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
        return delegate.build();
    }
}
