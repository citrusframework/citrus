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

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.actions.http.HttpSendRequestMessageBuilderFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.http.message.HttpMessageUtils;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.builder.SendMessageBuilderSupport;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

/**
 * @since 2.4
 */
public class HttpClientRequestActionBuilder extends
        SendMessageAction.SendMessageActionBuilder<SendMessageAction, HttpClientRequestActionBuilder.HttpMessageBuilderSupport, HttpClientRequestActionBuilder>
        implements org.citrusframework.actions.http.HttpClientRequestActionBuilder<SendMessageAction, HttpClientRequestActionBuilder.HttpMessageBuilderSupport, HttpClientRequestActionBuilder> {

    /**
     * Http message to send or receive
     */
    private final HttpMessage httpMessage;

    /**
     * Default constructor initializes http message.
     */
    public HttpClientRequestActionBuilder() {
        this.httpMessage = new HttpMessage();
        message(new HttpMessageBuilder(httpMessage));
    }

    /**
     * Subclasses may use custom message builder and Http message.
     */
    protected HttpClientRequestActionBuilder(MessageBuilder messageBuilder,
                                             HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        message(messageBuilder);
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

    @Override
    public HttpClientRequestActionBuilder path(String path) {
        httpMessage.path(path);
        return this;
    }

    /**
     * Sets the request method.
     */
    public HttpClientRequestActionBuilder method(HttpMethod method) {
        httpMessage.method(method);
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder method(String method) {
        method(HttpMethod.valueOf(method));
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder uri(String uri) {
        httpMessage.uri(uri);
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder queryParam(String name) {
        httpMessage.queryParam(name, null);
        return this;
    }

    @Override
    public HttpClientRequestActionBuilder queryParam(String name, String value) {
        httpMessage.queryParam(name, value);
        return this;
    }

    public static class HttpMessageBuilderSupport extends
            SendMessageBuilderSupport<SendMessageAction, HttpClientRequestActionBuilder, HttpMessageBuilderSupport>
            implements HttpSendRequestMessageBuilderFactory<SendMessageAction, HttpMessageBuilderSupport> {

        private final HttpMessage httpMessage;

        protected HttpMessageBuilderSupport(HttpMessage httpMessage,
                                            HttpClientRequestActionBuilder delegate) {
            super(delegate);
            this.httpMessage = httpMessage;
        }

        @Override
        public HttpMessageBuilderSupport body(String payload) {
            httpMessage.setPayload(payload);
            return this;
        }

        /**
         * Adds message payload multi value map data to this builder. This is used when using
         * multipart file upload via Spring RestTemplate.
         */
        public HttpMessageBuilderSupport body(MultiValueMap<String, Object> payload) {
            httpMessage.setPayload(payload);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport body(Map<String, List<Object>> payload) {
            body(MultiValueMap.fromMultiValue(payload));
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
         */
        public HttpMessageBuilderSupport method(HttpMethod method) {
            delegate.method(method.name());
            return this;
        }

        @Override
        public HttpMessageBuilderSupport method(String method) {
            delegate.method(method);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport uri(String uri) {
            delegate.uri(uri);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport queryParam(String name) {
            delegate.queryParam(name, null);
            return this;
        }

        @Override
        public HttpMessageBuilderSupport queryParam(String name, String value) {
            delegate.queryParam(name, value);
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
        public HttpMessageBuilderSupport accept(String accept) {
            httpMessage.accept(accept);
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
         * Adds cookie to response by "Cookie" header.
         */
        public HttpMessageBuilderSupport cookie(Cookie cookie) {
            httpMessage.cookie(cookie);
            return this;
        }
    }

    @Override
    public SendMessageAction doBuild() {
        return createSendMessageAction();
    }

    /**
     * Creates the actual SendMessageAction. Subclasses may override this method to provide specific
     * implementations.
     */
    protected SendMessageAction createSendMessageAction() {
        return new SendMessageAction(this);
    }
}
