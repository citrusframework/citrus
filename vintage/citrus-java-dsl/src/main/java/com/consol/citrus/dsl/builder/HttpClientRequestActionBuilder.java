package com.consol.citrus.dsl.builder;

import javax.servlet.http.Cookie;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

/**
 * @author Christoph Deppisch
 */
public class HttpClientRequestActionBuilder extends SendMessageActionBuilder<HttpClientRequestActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<SendMessageAction> {

    private final com.consol.citrus.http.actions.HttpClientRequestActionBuilder delegate;

    public HttpClientRequestActionBuilder(com.consol.citrus.http.actions.HttpClientRequestActionBuilder builder) {
        super(builder);
        this.delegate = builder;
    }

    /**
     * Adds message payload multi value map data to this builder. This is used when using multipart file upload via
     * Spring RestTemplate.
     * @param payload
     * @return
     */
    public HttpClientRequestActionBuilder payload(MultiValueMap<String,Object> payload) {
        delegate.payload(payload);
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder messageName(String name) {
        delegate.messageName(name);
        return super.messageName(name);
    }

    /**
     * Sets the request path.
     * @param path
     * @return
     */
    public HttpClientRequestActionBuilder path(String path) {
        delegate.path(path);
        return this;
    }

    /**
     * Sets the request method.
     * @param method
     * @return
     */
    public HttpClientRequestActionBuilder method(HttpMethod method) {
        delegate.method(method);
        return this;
    }

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    public HttpClientRequestActionBuilder uri(String uri) {
        delegate.uri(uri);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @return
     */
    public HttpClientRequestActionBuilder queryParam(String name) {
        delegate.queryParam(name);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public HttpClientRequestActionBuilder queryParam(String name, String value) {
        delegate.queryParam(name, value);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpClientRequestActionBuilder version(String version) {
        delegate.version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpClientRequestActionBuilder contentType(String contentType) {
        delegate.contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public HttpClientRequestActionBuilder accept(String accept) {
        delegate.accept(accept);
        return this;
    }

    /**
     * Adds cookie to response by "Cookie" header.
     * @param cookie
     * @return
     */
    public HttpClientRequestActionBuilder cookie(Cookie cookie) {
        delegate.cookie(cookie);
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder message(Message message) {
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
