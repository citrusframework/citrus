package com.consol.citrus.dsl.builder;

import javax.servlet.http.Cookie;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class HttpClientResponseActionBuilder extends ReceiveMessageActionBuilder<HttpClientResponseActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<ReceiveMessageAction> {

    private final com.consol.citrus.http.actions.HttpClientResponseActionBuilder delegate;

    public HttpClientResponseActionBuilder(com.consol.citrus.http.actions.HttpClientResponseActionBuilder builder) {
        super(builder);
        this.delegate = builder;
    }

    @Override
    public HttpClientResponseActionBuilder payload(String payload) {
        delegate.message().body(payload);
        return this;
    }

    @Override
    public HttpClientResponseActionBuilder messageName(String name) {
        delegate.message().name(name);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public HttpClientResponseActionBuilder status(HttpStatus status) {
        delegate.message().status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public HttpClientResponseActionBuilder statusCode(Integer statusCode) {
        delegate.message().statusCode(statusCode);
        return this;
    }

    /**
     * Sets the response reason phrase.
     * @param reasonPhrase
     * @return
     */
    public HttpClientResponseActionBuilder reasonPhrase(String reasonPhrase) {
        delegate.message().reasonPhrase(reasonPhrase);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpClientResponseActionBuilder version(String version) {
        delegate.message().version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpClientResponseActionBuilder contentType(String contentType) {
        delegate.message().contentType(contentType);
        return this;
    }

    /**
     * Expects cookie on response via "Set-Cookie" header.
     * @param cookie
     * @return
     */
    public HttpClientResponseActionBuilder cookie(Cookie cookie) {
        delegate.message().cookie(cookie);
        return this;
    }

    @Override
    public HttpClientResponseActionBuilder message(Message message) {
        delegate.message(message);
        return this;
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
