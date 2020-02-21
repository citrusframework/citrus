/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.DataDictionary;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;

public class HttpMessageContentBuilder extends AbstractMessageContentBuilder {

    private final HttpMessage template;
    private final AbstractMessageContentBuilder delegate;
    private final CookieEnricher cookieEnricher;

    /**
     * Default constructor using fields.
     * @param httpMessage The template http message to use for message creation
     * @param delegate The message builder to use for message creation
     */
    public HttpMessageContentBuilder(final HttpMessage httpMessage,
                                     final AbstractMessageContentBuilder delegate) {
        this(httpMessage, delegate, new CookieEnricher());
    }

    /**
     * Constructor allowing the configuration of the cookie enricher.
     * Currently for testing purposes only
     * @param template The template http message to use for message creation
     * @param delegate The message builder to use for message creation
     * @param cookieEnricher The cookie enricher to use for message creation
     */
    HttpMessageContentBuilder(final HttpMessage template,
                              final AbstractMessageContentBuilder delegate,
                              final CookieEnricher cookieEnricher) {
        this.template = template;
        this.delegate = delegate;
        this.cookieEnricher = cookieEnricher;
    }

    @Override
    public Message buildMessageContent(final TestContext context, final String messageType, final MessageDirection direction) {
        //Copy the initial message, so that it is not manipulated during the test.
        final HttpMessage message = new HttpMessage(template);

        delegate.getMessageHeaders().putAll(template.getHeaders());
        final Message constructed = delegate.buildMessageContent(context, messageType, direction);

        message.setName(delegate.getMessageName());
        message.setPayload(constructed.getPayload());
        message.setCookies(constructCookies(context));
        replaceHeaders(constructed, message);

        return message;
    }

    /**
     * Replaces all headers
     * @param from The message to take the headers from
     * @param to The message to set the headers to
     */
    private void replaceHeaders(final Message from, final Message to) {
        to.getHeaders().clear();
        to.getHeaders().putAll(from.getHeaders());
    }

    /**
     * Replaces the dynamic content in the given list of cookies
     * @param context The context to replace the variables with
     */
    private Cookie[] constructCookies(final TestContext context) {
        final List<Cookie> cookies = cookieEnricher.enrich(template.getCookies(), context);
        return cookies.toArray(new Cookie[0]);
    }

    @Override
    public DataDictionary getDataDictionary() {
        return delegate.getDataDictionary();
    }

    @Override
    public List<MessageConstructionInterceptor> getMessageInterceptors() {
        return delegate.getMessageInterceptors();
    }

    @Override
    public void setMessageInterceptors(final List<MessageConstructionInterceptor> messageInterceptors) {
        delegate.setMessageInterceptors(messageInterceptors);
    }

    @Override
    public String getMessageName() {
        return delegate.getMessageName();
    }

    @Override
    public void setMessageName(final String messageName) {
        delegate.setMessageName(messageName);
    }

    @Override
    public void setMessageHeaders(final Map<String, Object> messageHeaders) {
        delegate.setMessageHeaders(messageHeaders);
    }

    @Override
    public List<String> getHeaderResources() {
        return delegate.getHeaderResources();
    }

    @Override
    public void setHeaderResources(final List<String> headerResources) {
        delegate.setHeaderResources(headerResources);
    }

    @Override
    public List<String> getHeaderData() {
        return delegate.getHeaderData();
    }

    @Override
    public void setHeaderData(final List<String> headerData) {
        delegate.setHeaderData(headerData);
    }

    @Override
    public Map<String, Object> getMessageHeaders() {
        return delegate.getMessageHeaders();
    }

    @Override
    public Object buildMessagePayload(final TestContext context, final String messageType) {
        return delegate.buildMessagePayload(context, messageType);
    }

    @Override
    public void add(final MessageConstructionInterceptor interceptor) {
        delegate.add(interceptor);
    }

    @Override
    public void setDataDictionary(final DataDictionary dataDictionary) {
        delegate.setDataDictionary(dataDictionary);
    }

    public AbstractMessageContentBuilder getDelegate() {
        return delegate;
    }

    public HttpMessage getMessage() {
        return template;
    }
}
