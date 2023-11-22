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
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class HttpClientRequestActionBuilder extends SendMessageAction.SendMessageActionBuilder<SendMessageAction, HttpClientRequestActionBuilder.HttpMessageBuilderSupport, HttpClientRequestActionBuilder> {

    /** Http message to send or receive */
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
     * @param messageBuilder
     * @param httpMessage
     */
    protected HttpClientRequestActionBuilder(MessageBuilder messageBuilder, HttpMessage httpMessage) {
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

    /**
     * Sets the request path.
     * @param path
     * @return
     */
    public HttpClientRequestActionBuilder path(String path) {
        httpMessage.path(path);
        return this;
    }

    /**
     * Sets the request method.
     * @param method
     * @return
     */
    public HttpClientRequestActionBuilder method(HttpMethod method) {
        httpMessage.method(method);
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
        httpMessage.uri(uri);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @return
     */
    public HttpClientRequestActionBuilder queryParam(String name) {
        httpMessage.queryParam(name, null);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public HttpClientRequestActionBuilder queryParam(String name, String value) {
        httpMessage.queryParam(name, value);
        return this;
    }

    public static class HttpMessageBuilderSupport extends SendMessageBuilderSupport<SendMessageAction, HttpClientRequestActionBuilder, HttpMessageBuilderSupport> {

        private final HttpMessage httpMessage;

        protected HttpMessageBuilderSupport(HttpMessage httpMessage, HttpClientRequestActionBuilder delegate) {
            super(delegate);
            this.httpMessage = httpMessage;
        }

        @Override
        public HttpMessageBuilderSupport body(String payload) {
            httpMessage.setPayload(payload);
            return this;
        }

        /**
         * Adds message payload multi value map data to this builder. This is used when using multipart file upload via
         * Spring RestTemplate.
         * @param payload
         * @return
         */
        public HttpMessageBuilderSupport body(MultiValueMap<String,Object> payload) {
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
        public HttpMessageBuilderSupport uri(String uri) {
            delegate.uri(uri);
            return this;
        }

        /**
         * Adds a query param to the request uri.
         * @param name
         * @return
         */
        public HttpMessageBuilderSupport queryParam(String name) {
            delegate.queryParam(name, null);
            return this;
        }

        /**
         * Adds a query param to the request uri.
         * @param name
         * @param value
         * @return
         */
        public HttpMessageBuilderSupport queryParam(String name, String value) {
            delegate.queryParam(name, value);
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
    public SendMessageAction doBuild() {
        return new SendMessageAction(this);
    }
}
