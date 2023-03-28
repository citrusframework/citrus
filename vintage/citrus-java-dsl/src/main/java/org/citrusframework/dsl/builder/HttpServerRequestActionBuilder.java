package org.citrusframework.dsl.builder;

import jakarta.servlet.http.Cookie;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.message.Message;
import org.springframework.http.HttpMethod;

/**
 * @author Christoph Deppisch
 */
public class HttpServerRequestActionBuilder extends ReceiveMessageActionBuilder<HttpServerRequestActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<ReceiveMessageAction> {

    private final org.citrusframework.http.actions.HttpServerRequestActionBuilder delegate;

    public HttpServerRequestActionBuilder(org.citrusframework.http.actions.HttpServerRequestActionBuilder builder) {
        super(builder);
        this.delegate = builder;
    }

    @Override
    public HttpServerRequestActionBuilder payload(String payload) {
        delegate.message().body(payload);
        return this;
    }

    @Override
    public HttpServerRequestActionBuilder messageName(String name) {
        delegate.message().name(name);
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
        delegate.message().method(method);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name) {
        delegate.message().queryParam(name, null);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name, String value) {
        delegate.message().queryParam(name, value);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpServerRequestActionBuilder version(String version) {
        delegate.message().version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpServerRequestActionBuilder contentType(String contentType) {
        delegate.message().contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public HttpServerRequestActionBuilder accept(String accept) {
        delegate.message().accept(accept);
        return this;
    }

    /**
     * Adds cookie to response by "Cookie" header.
     * @param cookie
     * @return
     */
    public HttpServerRequestActionBuilder cookie(Cookie cookie) {
        delegate.message().cookie(cookie);
        return this;
    }

    @Override
    public HttpServerRequestActionBuilder message(Message message) {
        delegate.message(message);
        return this;
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
