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

package com.consol.citrus.ws.message;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.ws.SoapAttachment;

import java.util.*;

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

    /** Optional list of header fragments */
    private List<String> headerFragments = new ArrayList<String>();

    /** Optional list of SOAP attachments */
    private List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();

    /**
     * Default constructor using just message payload.
     *
     * @param payload
     */
    public SoapMessage(Object payload) {
        this(payload, new LinkedHashMap<String, Object>());
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

    @Override
    public SoapMessage setHeader(String headerName, Object headerValue) {
        return (SoapMessage) super.setHeader(headerName, headerValue);
    }

    /**
     * Set soap action for this message.
     * @param soapAction
     */
    public SoapMessage setSoapAction(String soapAction) {
        setHeader(SoapMessageHeaders.SOAP_ACTION, soapAction);
        return this;
    }

    /**
     * Gets the soap action for this message.
     * @return
     */
    public String getSoapAction() {
        return getHeader(SoapMessageHeaders.SOAP_ACTION).toString();
    }

    /**
     * Adds new header fragment data.
     * @param headerData
     * @return
     */
    public SoapMessage addHeaderFragment(String headerData) {
        this.headerFragments.add(headerData);
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
     * Gets the list of header fragments in this message.
     * @return
     */
    public List<String> getHeaderFragments() {
        return headerFragments;
    }

    /**
     * Gets the list of message attachments.
     * @return
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }
}
