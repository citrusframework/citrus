package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.http.actions.HttpClientActionBuilder;
import com.consol.citrus.http.actions.HttpServerActionBuilder;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class HttpActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final com.consol.citrus.http.actions.HttpActionBuilder delegate = new com.consol.citrus.http.actions.HttpActionBuilder();

    /**
     * Initiate http client action.
     */
    public HttpClientActionBuilder client(HttpClient httpClient) {
        return delegate.client(httpClient);
    }

    public HttpClientActionBuilder client(String httpClient) {
        return delegate.client(httpClient);
    }

    public HttpServerActionBuilder server(HttpServer httpServer) {
        return delegate.server(httpServer);
    }

    public HttpServerActionBuilder server(String httpServer) {
        return delegate.server(httpServer);
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
}
