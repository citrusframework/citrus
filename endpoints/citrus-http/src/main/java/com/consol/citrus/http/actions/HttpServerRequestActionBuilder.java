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
import com.consol.citrus.http.message.HttpMessageBuilder;
import com.consol.citrus.http.message.HttpMessageUtils;
import com.consol.citrus.http.message.HttpQueryParamHeaderValidator;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.builder.MessageBuilderSupport;
import org.springframework.http.HttpMethod;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerRequestActionBuilder extends ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, HttpServerRequestActionBuilder.HttpMessageBuilderSupport, HttpServerRequestActionBuilder> {

    /** Http message to send or receive */
    private final HttpMessage httpMessage = new HttpMessage();

    /**
     * Default constructor.
     */
    public HttpServerRequestActionBuilder() {
        message(new HttpMessageBuilder(httpMessage))
            .headerNameIgnoreCase(true);
        validator(new HttpQueryParamHeaderValidator());
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

    @Override
    public HttpMessageBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new HttpMessageBuilderSupport(httpMessage, this);
        }
        return super.getMessageBuilderSupport();
    }

    public static class HttpMessageBuilderSupport extends MessageBuilderSupport<ReceiveMessageAction, HttpServerRequestActionBuilder, HttpMessageBuilderSupport> {

        private final HttpMessage httpMessage;

        protected HttpMessageBuilderSupport(HttpMessage httpMessage, HttpServerRequestActionBuilder delegate) {
            super(delegate);
            this.httpMessage = httpMessage;
        }

        @Override
        public HttpMessageBuilderSupport body(String payload) {
            httpMessage.setPayload(payload);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport name(String name) {
            httpMessage.setName(name);
            return super.name(name);
        }

        @Override
        public HttpMessageBuilderSupport from(Message controlMessage) {
            HttpMessageUtils.copy(controlMessage, httpMessage);
            return this;
        }

        /**
         * Sets the request method.
         * @param method
         * @return
         */
        public HttpMessageBuilderSupport method(HttpMethod method) {
            httpMessage.method(method);
            return this;
        }

        /**
         * Adds a query param to the request uri.
         * @param name
         * @return
         */
        public HttpMessageBuilderSupport queryParam(String name) {
            httpMessage.queryParam(name, null);
            return this;
        }

        /**
         * Adds a query param to the request uri.
         * @param name
         * @param value
         * @return
         */
        public HttpMessageBuilderSupport queryParam(String name, String value) {
            httpMessage.queryParam(name, value);
            return this;
        }

        /**
         * Sets the http version.
         * @param version
         * @return
         */
        public HttpMessageBuilderSupport version(String version) {
            httpMessage.version(version);
            return this;
        }

        /**
         * Sets the request content type header.
         * @param contentType
         * @return
         */
        public HttpMessageBuilderSupport contentType(String contentType) {
            httpMessage.contentType(contentType);
            return this;
        }

        /**
         * Sets the request accept header.
         * @param accept
         * @return
         */
        public HttpMessageBuilderSupport accept(String accept) {
            httpMessage.accept(accept);
            return this;
        }

        /**
         * Adds cookie to response by "Cookie" header.
         * @param cookie
         * @return
         */
        public HttpMessageBuilderSupport cookie(Cookie cookie) {
            httpMessage.cookie(cookie);
            return this;
        }

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
