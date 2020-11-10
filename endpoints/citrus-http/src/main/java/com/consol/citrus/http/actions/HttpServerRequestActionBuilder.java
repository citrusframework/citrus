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

package com.consol.citrus.http.actions;

import javax.servlet.http.Cookie;
import java.util.Optional;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageContentBuilder;
import com.consol.citrus.http.message.HttpMessageUtils;
import com.consol.citrus.http.message.HttpQueryParamHeaderValidator;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpMethod;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerRequestActionBuilder extends ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, HttpServerRequestActionBuilder> {

    /** Http message to send or receive */
    private HttpMessage httpMessage = new HttpMessage();

    /**
     * Default constructor.
     */
    public HttpServerRequestActionBuilder() {
        message(new HttpMessageContentBuilder(httpMessage));
        headerNameIgnoreCase(true);
        validator(new HttpQueryParamHeaderValidator());
    }

    @Override
    public HttpServerRequestActionBuilder payload(String payload) {
        httpMessage.setPayload(payload);
        return this;
    }

    @Override
    public HttpServerRequestActionBuilder messageName(String name) {
        httpMessage.setName(name);
        return super.messageName(name);
    }

    /**
     * Sets the request path.
     * @param path
     * @return
     */
    public HttpServerRequestActionBuilder path(String path) {
        httpMessage.path(path);
        return this;
    }

    /**
     * Sets the request method.
     * @param method
     * @return
     */
    public HttpServerRequestActionBuilder method(HttpMethod method) {
        httpMessage.method(method);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name) {
        httpMessage.queryParam(name, null);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public HttpServerRequestActionBuilder queryParam(String name, String value) {
        httpMessage.queryParam(name, value);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpServerRequestActionBuilder version(String version) {
        httpMessage.version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpServerRequestActionBuilder contentType(String contentType) {
        httpMessage.contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public HttpServerRequestActionBuilder accept(String accept) {
        httpMessage.accept(accept);
        return this;
    }

    /**
     * Adds cookie to response by "Cookie" header.
     * @param cookie
     * @return
     */
    public HttpServerRequestActionBuilder cookie(Cookie cookie) {
        httpMessage.cookie(cookie);
        return this;
    }

    @Override
    public HttpServerRequestActionBuilder message(Message message) {
        HttpMessageUtils.copy(message, httpMessage);
        return this;
    }

    @Override
    public ReceiveMessageAction doBuild() {
        return new ReceiveMessageAction(this);
    }

    @Override
    protected Optional<String> getMessagePayload() {
        if (httpMessage.getPayload() instanceof String) {
            return Optional.of(httpMessage.getPayload(String.class));
        }

        return super.getMessagePayload();
    }
}
