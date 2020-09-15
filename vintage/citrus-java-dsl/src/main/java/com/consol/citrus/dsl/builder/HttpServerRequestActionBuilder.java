package com.consol.citrus.dsl.builder;

import javax.servlet.http.Cookie;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpMethod;

/**
 * @author Christoph Deppisch
 */
public class HttpServerRequestActionBuilder extends ReceiveMessageActionBuilder<HttpServerRequestActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<ReceiveMessageAction> {

    private final com.consol.citrus.http.actions.HttpServerRequestActionBuilder delegate;

    public HttpServerRequestActionBuilder(com.consol.citrus.http.actions.HttpServerRequestActionBuilder builder) {
        super(builder);
        this.delegate = builder;
    }

    @Override
    public HttpServerRequestActionBuilder messageName(String name) {
        delegate.messageName(name);
        return this;
    }

    /**
     * Sets the request path.
     * @param path
     * @return
     */
    public HttpServerRequestActionBuilder path(String path) {
        delegate.path(path);
        return this;
    }

    /**
     * Sets the request method.
     * @param method
     * @return
     */
    public HttpServerRequestActionBuilder method(HttpMethod method) {
        delegate.method(method);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name) {
        delegate.queryParam(name, null);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name, String value) {
        delegate.queryParam(name, value);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpServerRequestActionBuilder version(String version) {
        delegate.version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpServerRequestActionBuilder contentType(String contentType) {
        delegate.contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public HttpServerRequestActionBuilder accept(String accept) {
        delegate.accept(accept);
        return this;
    }

    /**
     * Adds cookie to response by "Cookie" header.
     * @param cookie
     * @return
     */
    public HttpServerRequestActionBuilder cookie(Cookie cookie) {
        delegate.cookie(cookie);
        return this;
    }

    @Override
    public HttpServerRequestActionBuilder message(Message message) {
        delegate.message(message);
        return this;
    }

    @Override
    public ReceiveMessageAction build() {
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
