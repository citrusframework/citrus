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
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.message.*;
import com.consol.citrus.message.*;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.springframework.http.HttpMethod;

import javax.servlet.http.Cookie;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerRequestActionBuilder extends ReceiveMessageBuilder<ReceiveMessageAction, HttpServerRequestActionBuilder> {

    /** Http message to send or receive */
    private HttpMessage httpMessage = new HttpMessage();

    /**
     * Default constructor using http client endpoint.
     * @param delegate
     * @param httpServer
     */
    public HttpServerRequestActionBuilder(DelegatingTestAction<TestAction> delegate, Endpoint httpServer) {
        super(delegate);
        delegate.setDelegate(new ReceiveMessageAction());
        getAction().setEndpoint(httpServer);
        initMessage(httpMessage);
        messageType(MessageType.XML);
        headerNameIgnoreCase(true);
        headerValidator(new HttpQueryParamHeaderValidator());
    }

    /**
     * Initialize message builder.
     * @param message
     */
    private void initMessage(HttpMessage message) {
        StaticMessageContentBuilder staticMessageContentBuilder = StaticMessageContentBuilder.withMessage(message);
        staticMessageContentBuilder.setMessageHeaders(message.getHeaders());
        getAction().setMessageBuilder(new HttpMessageContentBuilder(message, staticMessageContentBuilder));
    }

    @Override
    protected void setPayload(String payload) {
        httpMessage.setPayload(payload);
    }

    @Override
    public HttpServerRequestActionBuilder name(String name) {
        httpMessage.setName(name);
        return super.name(name);
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
}
