package com.consol.citrus.dsl.builder;

import javax.servlet.http.Cookie;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class HttpServerResponseActionBuilder extends SendMessageActionBuilder<HttpServerResponseActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<SendMessageAction> {

    private final com.consol.citrus.http.actions.HttpServerResponseActionBuilder delegate;

    public HttpServerResponseActionBuilder(com.consol.citrus.http.actions.HttpServerResponseActionBuilder builder) {
        super(builder);
        this.delegate = builder;
    }

    @Override
    public HttpServerResponseActionBuilder payload(String payload) {
        delegate.message().body(payload);
        return this;
    }

    @Override
    public HttpServerResponseActionBuilder messageName(String name) {
        delegate.message().name(name);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public HttpServerResponseActionBuilder status(HttpStatus status) {
        delegate.message().status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public HttpServerResponseActionBuilder statusCode(Integer statusCode) {
        delegate.message().statusCode(statusCode);
        return this;
    }

    /**
     * Sets the response reason phrase.
     * @param reasonPhrase
     * @return
     */
    public HttpServerResponseActionBuilder reasonPhrase(String reasonPhrase) {
        delegate.message().reasonPhrase(reasonPhrase);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpServerResponseActionBuilder version(String version) {
        delegate.message().version(version);
        return this;
    }

    /**
     * Sets the response content type header.
     * @param contentType
     * @return
     */
    public HttpServerResponseActionBuilder contentType(String contentType) {
        delegate.message().contentType(contentType);
        return this;
    }

    /**
     * Adds cookie to response by "Set-Cookie" header.
     * @param cookie
     * @return
     */
    public HttpServerResponseActionBuilder cookie(Cookie cookie) {
        delegate.message().cookie(cookie);
        return this;
    }

    @Override
    public HttpServerResponseActionBuilder message(Message message) {
        delegate.message(message);
        return this;
    }

    @Override
    public SendMessageAction build() {
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
