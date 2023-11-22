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

package org.citrusframework.http.actions;

import jakarta.servlet.http.Cookie;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageUtils;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.builder.SendMessageBuilderSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpServerResponseActionBuilder extends SendMessageAction.SendMessageActionBuilder<SendMessageAction, HttpServerResponseActionBuilder.HttpMessageBuilderSupport, HttpServerResponseActionBuilder> {

    /** Http message to send or receive */
    private final HttpMessage httpMessage;

    /**
     * Default constructor.
     */
    public HttpServerResponseActionBuilder() {
        this.httpMessage = new HttpMessage();
        message(new HttpMessageBuilder(httpMessage));
    }

    /**
     * Subclasses may use custom message builder and Http message.
     * @param messageBuilder
     * @param httpMessage
     */
    public HttpServerResponseActionBuilder(MessageBuilder messageBuilder, HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        message(messageBuilder);
    }

    @Override
    public HttpMessageBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new HttpMessageBuilderSupport(httpMessage, this);
        }
        return super.getMessageBuilderSupport();
    }

    public static class HttpMessageBuilderSupport extends SendMessageBuilderSupport<SendMessageAction, HttpServerResponseActionBuilder, HttpMessageBuilderSupport> {

        private final HttpMessage httpMessage;

        protected HttpMessageBuilderSupport(HttpMessage httpMessage, HttpServerResponseActionBuilder delegate) {
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
         * Sets the response status.
         * @param status
         * @return
         */
        public HttpMessageBuilderSupport status(HttpStatus status) {
            httpMessage.status(status);
            return this;
        }

        /**
         * Sets the response status code.
         * @param statusCode
         * @return
         */
        public HttpMessageBuilderSupport statusCode(Integer statusCode) {
            httpMessage.status(HttpStatusCode.valueOf(statusCode));
            return this;
        }

        /**
         * Sets the response reason phrase.
         * @param reasonPhrase
         * @return
         */
        public HttpMessageBuilderSupport reasonPhrase(String reasonPhrase) {
            httpMessage.reasonPhrase(reasonPhrase);
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
         * Sets the response content type header.
         * @param contentType
         * @return
         */
        public HttpMessageBuilderSupport contentType(String contentType) {
            httpMessage.contentType(contentType);
            return this;
        }

        /**
         * Adds cookie to response by "Set-Cookie" header.
         * @param cookie
         * @return
         */
        public HttpMessageBuilderSupport cookie(Cookie cookie) {
            httpMessage.cookie(cookie);
            return this;
        }
    }

    @Override
    public SendMessageAction doBuild() {
        return new SendMessageAction(this);
    }
}
