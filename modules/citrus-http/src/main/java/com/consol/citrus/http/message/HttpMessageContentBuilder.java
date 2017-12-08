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
import com.consol.citrus.message.*;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.DataDictionary;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HttpMessageContentBuilder extends AbstractMessageContentBuilder {

    private final HttpMessage template;
    private final AbstractMessageContentBuilder delegate;

    /**
     * Default constructor using fields.
     * @param httpMessage
     * @param delegate
     */
    public HttpMessageContentBuilder(HttpMessage httpMessage, AbstractMessageContentBuilder delegate) {
        this.template = httpMessage;
        this.delegate = delegate;
    }

    @Override
    public Message buildMessageContent(TestContext context, String messageType) {
        return buildMessageContent(context, messageType, MessageDirection.UNBOUND);
    }

    @Override
    public Message buildMessageContent(TestContext context, String messageType, MessageDirection direction) {
        //Copy the initial message, so that it is not manipulated during the test.
        HttpMessage message = new HttpMessage(template);

        delegate.getMessageHeaders().putAll(template.getHeaders());
        Message constructed = delegate.buildMessageContent(context, messageType, direction);

        message.setName(delegate.getMessageName());
        message.setPayload(constructed.getPayload());
        message.setCookies(constructCookies(context));
        copyHeaders(constructed, message);

        return message;
    }

    /**
     * Copies all headers except id and timestamp
     * @param from The message to copy the headers from
     * @param to The message to set the headers to
     */
    private void copyHeaders(Message from, Message to) {
        for (Map.Entry<String, Object> headerEntry : from.getHeaders().entrySet()) {
            if (notIdOrTimestamp(headerEntry.getKey())) {
                to.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
    }

    /**
     * Checks whether the given message header is not an ID or a TIMESTAMP
     * @param messageHeader The message header to be checked
     * @return whether the given message header is not an ID or a TIMESTAMP
     */
    private boolean notIdOrTimestamp(String messageHeader) {
        return !(MessageHeaders.ID.equals(messageHeader) ||
                 MessageHeaders.TIMESTAMP.equals(messageHeader));
    }

    /**
     * Replaces the dynamic content in the given list of cookies
     * @param context The context to replace the variables with
     */
    private Cookie[] constructCookies(TestContext context) {
        List<Cookie> cookies = new ArrayList<>();

        for (Cookie cookie: template.getCookies()) {
            Cookie constructed = new Cookie(cookie.getName(), cookie.getValue());

            if (cookie.getValue() != null) {
                constructed.setValue(context.replaceDynamicContentInString(cookie.getValue()));
            }

            if (cookie.getComment() != null) {
                constructed.setComment(context.replaceDynamicContentInString(cookie.getComment()));
            }

            if (cookie.getPath() != null) {
                constructed.setPath(context.replaceDynamicContentInString(cookie.getPath()));
            }

            if (cookie.getDomain() != null) {
                constructed.setDomain(context.replaceDynamicContentInString(cookie.getDomain()));
            }
            
            constructed.setMaxAge(cookie.getMaxAge());
            constructed.setVersion(cookie.getVersion());
            constructed.setHttpOnly(cookie.isHttpOnly());
            constructed.setSecure(cookie.getSecure());

            cookies.add(constructed);
        }

        return cookies.toArray(new Cookie[cookies.size()]);
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
    public void setMessageInterceptors(List<MessageConstructionInterceptor> messageInterceptors) {
        delegate.setMessageInterceptors(messageInterceptors);
    }

    @Override
    public String getMessageName() {
        return delegate.getMessageName();
    }

    @Override
    public void setMessageName(String messageName) {
        delegate.setMessageName(messageName);
    }

    @Override
    public void setMessageHeaders(Map<String, Object> messageHeaders) {
        delegate.setMessageHeaders(messageHeaders);
    }

    @Override
    public List<String> getHeaderResources() {
        return delegate.getHeaderResources();
    }

    @Override
    public void setHeaderResources(List<String> headerResources) {
        delegate.setHeaderResources(headerResources);
    }

    @Override
    public List<String> getHeaderData() {
        return delegate.getHeaderData();
    }

    @Override
    public void setHeaderData(List<String> headerData) {
        delegate.setHeaderData(headerData);
    }

    @Override
    public Map<String, Object> getMessageHeaders() {
        return delegate.getMessageHeaders();
    }

    @Override
    public Object buildMessagePayload(TestContext context, String messageType) {
        return delegate.buildMessagePayload(context, messageType);
    }

    @Override
    public void add(MessageConstructionInterceptor interceptor) {
        delegate.add(interceptor);
    }

    @Override
    public void setDataDictionary(DataDictionary dataDictionary) {
        delegate.setDataDictionary(dataDictionary);
    }

    /**
     * Gets the delegate.
     *
     * @return
     */
    public AbstractMessageContentBuilder getDelegate() {
        return delegate;
    }

    /**
     * Gets the message.
     *
     * @return
     */
    public HttpMessage getMessage() {
        return template;
    }
}
