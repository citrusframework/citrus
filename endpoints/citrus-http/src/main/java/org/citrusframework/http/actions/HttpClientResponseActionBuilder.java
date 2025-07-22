/*
 * Copyright the original author or authors.
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

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.http.HttpReceiveResponseMessageBuilderFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageUtils;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.builder.ReceiveMessageBuilderSupport;
import org.springframework.http.HttpStatusCode;

/**
 * @since 2.4
 */
public class HttpClientResponseActionBuilder extends
        ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder.HttpMessageBuilderSupport, HttpClientResponseActionBuilder>
        implements org.citrusframework.actions.http.HttpClientResponseActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder.HttpMessageBuilderSupport> {

    /** Http message to send or receive */
    private final HttpMessage httpMessage;

    /**
     * Default constructor.
     */
    public HttpClientResponseActionBuilder() {
        this.httpMessage = new HttpMessage();
        message(new HttpMessageBuilder(httpMessage))
                .headerNameIgnoreCase(true);
    }

    /**
     * Subclasses may use custom message builder and Http message.
     */
    public HttpClientResponseActionBuilder(MessageBuilder messageBuilder, HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        message(messageBuilder)
                .headerNameIgnoreCase(true);
    }

    @Override
    public HttpMessageBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = createHttpMessageBuilderSupport();
        }
        return super.getMessageBuilderSupport();
    }

    protected HttpMessageBuilderSupport createHttpMessageBuilderSupport() {
        return new HttpMessageBuilderSupport(httpMessage, this);
    }

    public static class HttpMessageBuilderSupport extends
            ReceiveMessageBuilderSupport<ReceiveMessageAction, HttpClientResponseActionBuilder, HttpMessageBuilderSupport>
            implements HttpReceiveResponseMessageBuilderFactory<ReceiveMessageAction, HttpMessageBuilderSupport> {

        private final HttpMessage httpMessage;

        protected HttpMessageBuilderSupport(HttpMessage httpMessage, HttpClientResponseActionBuilder delegate) {
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
         */
        public HttpMessageBuilderSupport status(HttpStatusCode status) {
            httpMessage.status(status);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport status(int status) {
            status(HttpStatusCode.valueOf(status));
            return this;
        }

        @Override
        public HttpMessageBuilderSupport statusCode(int statusCode) {
            httpMessage.status(HttpStatusCode.valueOf(statusCode));
            return this;
        }

        @Override
        public HttpMessageBuilderSupport reasonPhrase(String reasonPhrase) {
            httpMessage.reasonPhrase(reasonPhrase);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport version(String version) {
            httpMessage.version(version);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport contentType(String contentType) {
            httpMessage.contentType(contentType);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport cookie(Object o) {
            if (o == null) {
                return this;
            }

            if (o instanceof Cookie cookie) {
                httpMessage.cookie(cookie);
            } else {
                throw new CitrusRuntimeException("Invalid cookie type: " + o.getClass());
            }

            return this;
        }

        /**
         * Expects cookie on response via "Set-Cookie" header.
         */
        public HttpMessageBuilderSupport cookie(Cookie cookie) {
            httpMessage.cookie(cookie);
            return this;
        }
    }

    @Override
    public ReceiveMessageAction doBuild() {
        return createReceiveMessageAction();
    }


    /**
     * Creates the actual ReceiveMessageAction. Subclasses may override this method to provide specific
     * implementations.
     */
    protected ReceiveMessageAction createReceiveMessageAction() {
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
