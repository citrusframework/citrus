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
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.http.message.*;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.springframework.http.HttpStatus;

import javax.servlet.http.Cookie;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerResponseActionBuilder extends SendMessageBuilder<SendMessageAction, HttpServerResponseActionBuilder> {

    /** Http message to send or receive */
    private HttpMessage httpMessage = new HttpMessage();

    /**
     * Default constructor using http client endpoint.
     * @param delegate
     * @param httpServer
     */
    public HttpServerResponseActionBuilder(DelegatingTestAction<TestAction> delegate, Endpoint httpServer) {
        super(delegate);
        delegate.setDelegate(new SendMessageAction());
        getAction().setEndpoint(httpServer);
        initMessage(httpMessage);
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
    public HttpServerResponseActionBuilder name(String name) {
        httpMessage.setName(name);
        return super.name(name);
    }
    
    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public HttpServerResponseActionBuilder status(HttpStatus status) {
        httpMessage.status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public HttpServerResponseActionBuilder statusCode(Integer statusCode) {
        httpMessage.statusCode(statusCode);
        return this;
    }

    /**
     * Sets the response reason phrase.
     * @param reasonPhrase
     * @return
     */
    public HttpServerResponseActionBuilder reasonPhrase(String reasonPhrase) {
        httpMessage.reasonPhrase(reasonPhrase);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpServerResponseActionBuilder version(String version) {
        httpMessage.version(version);
        return this;
    }

    /**
     * Sets the response content type header.
     * @param contentType
     * @return
     */
    public HttpServerResponseActionBuilder contentType(String contentType) {
        httpMessage.contentType(contentType);
        return this;
    }

    /**
     * Adds cookie to response by "Set-Cookie" header.
     * @param cookie
     * @return
     */
    public HttpServerResponseActionBuilder cookie(Cookie cookie) {
        httpMessage.cookie(cookie);
        return this;
    }

    @Override
    public HttpServerResponseActionBuilder message(Message message) {
        HttpMessageUtils.copy(message, httpMessage);
        return this;
    }
}
