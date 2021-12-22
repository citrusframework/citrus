package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class HttpActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final com.consol.citrus.http.actions.HttpActionBuilder delegate = new com.consol.citrus.http.actions.HttpActionBuilder();

    /**
     * Initiate http client action.
     */
    public HttpClientActionBuilder client(HttpClient httpClient) {
        return new HttpClientActionBuilder(delegate.client(httpClient));
    }

    public HttpClientActionBuilder client(String httpClient) {
        return new HttpClientActionBuilder(delegate.client(httpClient));
    }

    public HttpServerActionBuilder server(Endpoint httpServer) {
        return new HttpServerActionBuilder(delegate.server(httpServer));
    }

    public HttpServerActionBuilder server(String httpServer) {
        return new HttpServerActionBuilder(delegate.server(httpServer));
    }

    public HttpActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public TestAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    public static class HttpServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {
        private final com.consol.citrus.http.actions.HttpServerActionBuilder   delegate;

        public HttpServerActionBuilder(com.consol.citrus.http.actions.HttpServerActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Generic response builder for sending response messages to client.
         * @return
         */
        public HttpServerResponseActionBuilder respond() {
            return new HttpServerResponseActionBuilder(delegate.respond());
        }

        /**
         * Generic response builder for sending response messages to client with response status code.
         * @return
         */
        public HttpServerResponseActionBuilder respond(HttpStatus status) {
            return new HttpServerResponseActionBuilder(delegate.respond(status));
        }

        /**
         * Receive Http requests as server.
         */
        public HttpServerReceiveActionBuilder receive() {
            return new HttpServerReceiveActionBuilder(delegate.receive());
        }

        /**
         * Send Http response messages as server to client.
         */
        public HttpServerSendActionBuilder send() {
            return new HttpServerSendActionBuilder(delegate.send());
        }

        /**
         * Sets the Spring bean application context.
         * @param referenceResolver
         */
        public HttpServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.withReferenceResolver(referenceResolver);
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.setReferenceResolver(referenceResolver);
        }

        @Override
        public TestAction build() {
            return delegate.build();
        }

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate.getDelegate();
        }
    }

    public static class HttpServerSendActionBuilder {
        private final com.consol.citrus.http.actions.HttpServerActionBuilder.HttpServerSendActionBuilder delegate;

        public HttpServerSendActionBuilder(com.consol.citrus.http.actions.HttpServerActionBuilder.HttpServerSendActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Generic response builder for sending response messages to client.
         * @return
         */
        public HttpServerResponseActionBuilder response() {
            return new HttpServerResponseActionBuilder(delegate.response());
        }

        /**
         * Generic response builder for sending response messages to client with response status code.
         * @return
         */
        public HttpServerResponseActionBuilder response(HttpStatus status) {
            return new HttpServerResponseActionBuilder(delegate.response(status));

        }
    }

    public static class HttpServerReceiveActionBuilder {
        private final com.consol.citrus.http.actions.HttpServerActionBuilder.HttpServerReceiveActionBuilder delegate;

        public HttpServerReceiveActionBuilder(com.consol.citrus.http.actions.HttpServerActionBuilder.HttpServerReceiveActionBuilder delegate) {
            this.delegate = delegate;
        }
        /**
         * Receive Http GET request as server.
         */
        public HttpServerRequestActionBuilder get() {
            return new HttpServerRequestActionBuilder(delegate.get());
        }

        /**
         * Receive Http GET request as server.
         */
        public HttpServerRequestActionBuilder get(String path) {
            return new HttpServerRequestActionBuilder(delegate.get(path));
        }

        /**
         * Receive Http POST request as server.
         */
        public HttpServerRequestActionBuilder post() {
            return new HttpServerRequestActionBuilder(delegate.post());
        }

        /**
         * Receive Http POST request as server.
         */
        public HttpServerRequestActionBuilder post(String path) {
            return new HttpServerRequestActionBuilder(delegate.post(path));
        }

        /**
         * Receive Http PUT request as server.
         */
        public HttpServerRequestActionBuilder put() {
            return new HttpServerRequestActionBuilder(delegate.put());
        }

        /**
         * Receive Http PUT request as server.
         */
        public HttpServerRequestActionBuilder put(String path) {
            return new HttpServerRequestActionBuilder(delegate.put(path));
        }

        /**
         * Receive Http DELETE request as server.
         */
        public HttpServerRequestActionBuilder delete() {
            return new HttpServerRequestActionBuilder(delegate.delete());
        }

        /**
         * Receive Http DELETE request as server.
         */
        public HttpServerRequestActionBuilder delete(String path) {
            return new HttpServerRequestActionBuilder(delegate.delete(path));
        }

        /**
         * Receive Http HEAD request as server.
         */
        public HttpServerRequestActionBuilder head() {
            return new HttpServerRequestActionBuilder(delegate.head());
        }

        /**
         * Receive Http HEAD request as server.
         */
        public HttpServerRequestActionBuilder head(String path) {
            return new HttpServerRequestActionBuilder(delegate.head(path));
        }

        /**
         * Receive Http OPTIONS request as server.
         */
        public HttpServerRequestActionBuilder options() {
            return new HttpServerRequestActionBuilder(delegate.options());
        }

        /**
         * Receive Http OPTIONS request as server.
         */
        public HttpServerRequestActionBuilder options(String path) {
            return new HttpServerRequestActionBuilder(delegate.options(path));
        }

        /**
         * Receive Http TRACE request as server.
         */
        public HttpServerRequestActionBuilder trace() {
            return new HttpServerRequestActionBuilder(delegate.trace());
        }

        /**
         * Receive Http TRACE request as server.
         */
        public HttpServerRequestActionBuilder trace(String path) {
            return new HttpServerRequestActionBuilder(delegate.trace(path));
        }

        /**
         * Receive Http PATCH request as server.
         */
        public HttpServerRequestActionBuilder patch() {
            return new HttpServerRequestActionBuilder(delegate.patch());
        }

        /**
         * Receive Http PATCH request as server.
         */
        public HttpServerRequestActionBuilder patch(String path) {
            return new HttpServerRequestActionBuilder(delegate.patch(path));
        }
    }

    public static class HttpClientActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {
        private final com.consol.citrus.http.actions.HttpClientActionBuilder delegate;

        public HttpClientActionBuilder(com.consol.citrus.http.actions.HttpClientActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Sends Http requests as client.
         */
        public HttpClientSendActionBuilder send() {
            return new HttpClientSendActionBuilder(delegate.send());
        }

        /**
         * Receives Http response messages as client.
         */
        public HttpClientReceiveActionBuilder receive() {
            return new HttpClientReceiveActionBuilder(delegate.receive());
        }

        /**
         * Sets the bean reference resolver.
         * @param referenceResolver
         */
        public HttpClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.withReferenceResolver(referenceResolver);
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.setReferenceResolver(referenceResolver);
        }

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate.getDelegate();
        }

        @Override
        public TestAction build() {
            return delegate.build();
        }
    }

    public static class HttpClientSendActionBuilder {
        private final com.consol.citrus.http.actions.HttpClientActionBuilder.HttpClientSendActionBuilder delegate;

        public HttpClientSendActionBuilder(com.consol.citrus.http.actions.HttpClientActionBuilder.HttpClientSendActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Sends Http GET request as client to server.
         */
        public HttpClientRequestActionBuilder get() {
            return new HttpClientRequestActionBuilder(delegate.get());
        }

        /**
         * Sends Http GET request as client to server.
         */
        public HttpClientRequestActionBuilder get(String path) {
            return new HttpClientRequestActionBuilder(delegate.get(path));
        }

        /**
         * Sends Http POST request as client to server.
         */
        public HttpClientRequestActionBuilder post() {
            return new HttpClientRequestActionBuilder(delegate.post());
        }

        /**
         * Sends Http POST request as client to server.
         */
        public HttpClientRequestActionBuilder post(String path) {
            return new HttpClientRequestActionBuilder(delegate.post(path));
        }

        /**
         * Sends Http PUT request as client to server.
         */
        public HttpClientRequestActionBuilder put() {
            return new HttpClientRequestActionBuilder(delegate.put());
        }

        /**
         * Sends Http PUT request as client to server.
         */
        public HttpClientRequestActionBuilder put(String path) {
            return new HttpClientRequestActionBuilder(delegate.put(path));
        }

        /**
         * Sends Http DELETE request as client to server.
         */
        public HttpClientRequestActionBuilder delete() {
            return new HttpClientRequestActionBuilder(delegate.delete());
        }

        /**
         * Sends Http DELETE request as client to server.
         */
        public HttpClientRequestActionBuilder delete(String path) {
            return new HttpClientRequestActionBuilder(delegate.delete(path));
        }

        /**
         * Sends Http HEAD request as client to server.
         */
        public HttpClientRequestActionBuilder head() {
            return new HttpClientRequestActionBuilder(delegate.head());
        }

        /**
         * Sends Http HEAD request as client to server.
         */
        public HttpClientRequestActionBuilder head(String path) {
            return new HttpClientRequestActionBuilder(delegate.head(path));
        }

        /**
         * Sends Http OPTIONS request as client to server.
         */
        public HttpClientRequestActionBuilder options() {
            return new HttpClientRequestActionBuilder(delegate.options());
        }

        /**
         * Sends Http OPTIONS request as client to server.
         */
        public HttpClientRequestActionBuilder options(String path) {
            return new HttpClientRequestActionBuilder(delegate.options(path));
        }

        /**
         * Sends Http TRACE request as client to server.
         */
        public HttpClientRequestActionBuilder trace() {
            return new HttpClientRequestActionBuilder(delegate.trace());
        }

        /**
         * Sends Http TRACE request as client to server.
         */
        public HttpClientRequestActionBuilder trace(String path) {
            return new HttpClientRequestActionBuilder(delegate.trace(path));
        }

        /**
         * Sends Http PATCH request as client to server.
         */
        public HttpClientRequestActionBuilder patch() {
            return new HttpClientRequestActionBuilder(delegate.patch());
        }

        /**
         * Sends Http PATCH request as client to server.
         */
        public HttpClientRequestActionBuilder patch(String path) {
            return new HttpClientRequestActionBuilder(delegate.patch(path));
        }
    }

    public static class HttpClientReceiveActionBuilder {
        private final com.consol.citrus.http.actions.HttpClientActionBuilder.HttpClientReceiveActionBuilder delegate;

        public HttpClientReceiveActionBuilder(com.consol.citrus.http.actions.HttpClientActionBuilder.HttpClientReceiveActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Generic response builder for expecting response messages on client.
         * @return
         */
        public HttpClientResponseActionBuilder response() {
            return new HttpClientResponseActionBuilder(delegate.response());
        }

        /**
         * Generic response builder for expecting response messages on client with response status code.
         * @return
         */
        public HttpClientResponseActionBuilder response(HttpStatus status) {
            return new HttpClientResponseActionBuilder(delegate.response(status));
        }
    }
}
