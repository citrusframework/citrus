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
import com.consol.citrus.message.Message;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpClientResponseActionBuilder extends ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder> {

    /** Http message to send or receive */
    private final HttpMessage httpMessage = new HttpMessage();

    /**
     * Default constructor.
     */
    public HttpClientResponseActionBuilder() {
        message(new HttpMessageContentBuilder(httpMessage));
        headerNameIgnoreCase(true);
    }

    @Override
    public HttpClientResponseActionBuilder payload(String payload) {
        httpMessage.setPayload(payload);
        return this;
    }

    @Override
    public HttpClientResponseActionBuilder messageName(String name) {
        httpMessage.setName(name);
        return super.messageName(name);
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public HttpClientResponseActionBuilder status(HttpStatus status) {
        httpMessage.status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public HttpClientResponseActionBuilder statusCode(Integer statusCode) {
        httpMessage.statusCode(statusCode);
        return this;
    }

    /**
     * Sets the response reason phrase.
     * @param reasonPhrase
     * @return
     */
    public HttpClientResponseActionBuilder reasonPhrase(String reasonPhrase) {
        httpMessage.reasonPhrase(reasonPhrase);
        return this;
    }

    /**
     * Sets the http version.
     * @param version
     * @return
     */
    public HttpClientResponseActionBuilder version(String version) {
        httpMessage.version(version);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public HttpClientResponseActionBuilder contentType(String contentType) {
        httpMessage.contentType(contentType);
        return this;
    }

    /**
     * Expects cookie on response via "Set-Cookie" header.
     * @param cookie
     * @return
     */
    public HttpClientResponseActionBuilder cookie(Cookie cookie) {
        httpMessage.cookie(cookie);
        return this;
    }

    @Override
    public HttpClientResponseActionBuilder message(Message message) {
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
