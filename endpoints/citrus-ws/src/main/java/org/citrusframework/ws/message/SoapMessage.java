/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ws.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.springframework.http.HttpStatus;

/**
 * SOAP message representation holding additional elements like SOAP action, header fragment data and
 * attachments.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapMessage extends DefaultMessage {

    /** Serial */
    private static final long serialVersionUID = 3289201140229458069L;

    /** Optional list of SOAP attachments */
    private final List<SoapAttachment> attachments = new ArrayList<>();

    /** enable/disable mtom attachments */
    private boolean mtomEnabled = false;

    /**
     * Empty constructor initializing with empty message payload.
     */
    public SoapMessage() {
        super();
    }

    /**
     * Constructs copy of given message.
     * @param message
     */
    public SoapMessage(Message message) {
        super(message);
    }

    /**
     * Default constructor using just message payload.
     *
     * @param payload
     */
    public SoapMessage(Object payload) {
        this(payload, new LinkedHashMap<>());
    }

    /**
     * Default constructor using payload and headers.
     *
     * @param payload
     * @param headers
     */
    public SoapMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Sets new header name value pair.
     * @param headerName
     * @param headerValue
     * @return
     */
    public SoapMessage header(String headerName, Object headerValue) {
        return (SoapMessage) super.setHeader(headerName, headerValue);
    }

    @Override
    public SoapMessage setHeader(String headerName, Object headerValue) {
        return (SoapMessage) super.setHeader(headerName, headerValue);
    }

    @Override
    public SoapMessage addHeaderData(String headerData) {
        return (SoapMessage) super.addHeaderData(headerData);
    }

    /**
     * Sets the Http request content type header.
     *
     * @param contentType The content type header value to use
     * @return The altered HttpMessage
     */
    public SoapMessage contentType(final String contentType) {
        setHeader(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Sets the Http accepted content type header for response.
     *
     * @param accept The accept header value to set
     * @return The altered HttpMessage
     */
    public SoapMessage accept(final String accept) {
        setHeader(SoapMessageHeaders.HTTP_ACCEPT, accept);
        return this;
    }



    /**
     * Sets the Http response status code.
     *
     * @param statusCode The status code header to respond with
     * @return The altered HttpMessage
     */
    public SoapMessage status(final HttpStatus statusCode) {
        statusCode(statusCode.value());
        reasonPhrase(statusCode.name());
        return this;
    }

    /**
     * Sets the Http response status code header.
     *
     * @param statusCode The status code header value to respond with
     * @return The altered HttpMessage
     */
    public SoapMessage statusCode(final Integer statusCode) {
        setHeader(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
        return this;
    }

    /**
     * Sets the Http response reason phrase header.
     *
     * @param reasonPhrase The reason phrase header value to use
     * @return The altered HttpMessage
     */
    public SoapMessage reasonPhrase(final String reasonPhrase) {
        setHeader(SoapMessageHeaders.HTTP_REASON_PHRASE, reasonPhrase);
        return this;
    }

    /**
     * Set soap action for this message.
     * @param soapAction
     */
    public SoapMessage soapAction(String soapAction) {
        setHeader(SoapMessageHeaders.SOAP_ACTION, soapAction);
        return this;
    }

    /**
     * Enable or disable mtom attachments
     * @param mtomEnabled
     */
    public SoapMessage mtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
        return this;
    }

    /**
     * Adds new attachment to this message.
     * @param attachment
     */
    public SoapMessage addAttachment(SoapAttachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    /**
     * Gets the soap action for this message.
     * @return
     */
    public String getSoapAction() {
        return Optional.ofNullable(getHeader(SoapMessageHeaders.SOAP_ACTION))
                .map(Object::toString)
                .orElse(null);
    }

    /**
     * Gets the list of message attachments.
     * @return
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Gets mtom attachments enabled
     * @return
     */
    public boolean isMtomEnabled() {
        return this.mtomEnabled;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[attachments: %s]", attachments);
    }
}
