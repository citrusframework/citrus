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
import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HttpMessageContentBuilder extends AbstractMessageContentBuilder {

    private final HttpMessage message;
    private final AbstractMessageContentBuilder delegate;

    /**
     * Default constructor using fields.
     * @param httpMessage
     * @param delegate
     */
    public HttpMessageContentBuilder(HttpMessage httpMessage, AbstractMessageContentBuilder delegate) {
        this.message = httpMessage;
        this.delegate = delegate;
    }

    @Override
    public Message buildMessageContent(TestContext context, String messageType) {
        return buildMessageContent(context, messageType, MessageDirection.UNBOUND);
    }

    @Override
    public Message buildMessageContent(TestContext context, String messageType, MessageDirection direction) {
        //Copy the initial message, so that it is not manipulated during the test.
        HttpMessage messageToBuild = new HttpMessage(message);

        delegate.setMessageHeaders(messageToBuild.getHeaders());
        Message delegateMessage = delegate.buildMessageContent(context, messageType, direction);

        messageToBuild.setName(delegate.getMessageName());
        messageToBuild.setPayload(delegateMessage.getPayload());
        copyHeaders(delegateMessage, messageToBuild);
        replaceDynamicValues(messageToBuild.getCookies(), context);

        return messageToBuild;
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
     * @param cookies The cookies in which the variables will be replaced
     * @param context The context to replace the variables with
     */
    private void replaceDynamicValues(List<Cookie> cookies, TestContext context) {
        for (Cookie cookie: cookies) {
            if (cookie.getValue() != null) {
                cookie.setValue(context.replaceDynamicContentInString(cookie.getValue()));
            }

            if (cookie.getComment() != null) {
                cookie.setComment(context.replaceDynamicContentInString(cookie.getComment()));
            }

            if (cookie.getPath() != null) {
                cookie.setPath(context.replaceDynamicContentInString(cookie.getPath()));
            }

            if (cookie.getDomain() != null) {
                cookie.setDomain(context.replaceDynamicContentInString(cookie.getDomain()));
            }
        }
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
        return message;
    }
}
