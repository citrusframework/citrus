/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.*;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapClientResponseActionBuilder extends ReceiveMessageBuilder<ReceiveSoapMessageAction, SoapClientResponseActionBuilder> {

    /** Http message to send or receive */
    private SoapMessage soapMessage = new SoapMessage();

    /**
     * Default constructor using soap client endpoint.
     * @param delegate
     * @param soapClient
     */
    public SoapClientResponseActionBuilder(DelegatingTestAction<TestAction> delegate, Endpoint soapClient) {
        super(delegate);
        delegate.setDelegate(new ReceiveSoapMessageAction());
        getAction().setEndpoint(soapClient);
        message(soapMessage);
        messageType(MessageType.XML);
        headerNameIgnoreCase(true);
    }

    /**
     * Default constructor using soap client uri.
     * @param delegate
     * @param soapClientUri
     */
    public SoapClientResponseActionBuilder(DelegatingTestAction<TestAction> delegate, String soapClientUri) {
        super(delegate);
        delegate.setDelegate(new ReceiveSoapMessageAction());
        getAction().setEndpointUri(soapClientUri);
        message(soapMessage);
        messageType(MessageType.XML);
        headerNameIgnoreCase(true);
    }

    @Override
    protected void setPayload(String payload) {
        soapMessage.setPayload(payload);
    }

    /**
     * Sets the control attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SoapClientResponseActionBuilder attachment(String contentId, String contentType, String content) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        attachment.setContent(content);

        getAction().getAttachments().add(attachment);

        return this;
    }

    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public SoapClientResponseActionBuilder attachment(String contentId, String contentType, Resource contentResource) {
        return attachment(contentId, contentType, contentResource, FileUtils.getDefaultCharset());
    }

    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @param charset
     * @return
     */
    public SoapClientResponseActionBuilder attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);

        try {
            attachment.setContent(FileUtils.readToString(contentResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment content resource", e);
        }

        getAction().getAttachments().add(attachment);

        return this;
    }

    /**
     * Sets the charset name for this send action builder's control attachment.
     * @param charsetName
     * @return
     */
    public SoapClientResponseActionBuilder charset(String charsetName) {
        if (!getAction().getAttachments().isEmpty()) {
            getAction().getAttachments().get(getAction().getAttachments().size() - 1).setCharsetName(charsetName);
        }
        return this;
    }

    /**
     * Sets the control attachment from Java object instance.
     * @param attachment
     * @return
     */
    public SoapClientResponseActionBuilder attachment(SoapAttachment attachment) {
        getAction().getAttachments().add(attachment);
        return this;
    }

    /**
     * Set explicit SOAP attachment validator.
     * @param validator
     * @return
     */
    public SoapClientResponseActionBuilder attachmentValidator(SoapAttachmentValidator validator) {
        getAction().setAttachmentValidator(validator);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public SoapClientResponseActionBuilder status(HttpStatus status) {
        soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, status.value());
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public SoapClientResponseActionBuilder statusCode(Integer statusCode) {
        soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
        return this;
    }

    /**
     * Sets the context path.
     * @param contextPath
     * @return
     */
    public SoapClientResponseActionBuilder contextPath(String contextPath) {
        soapMessage.header(SoapMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public SoapClientResponseActionBuilder contentType(String contentType) {
        soapMessage.header(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    protected ReceiveSoapMessageAction getAction() {
        return (ReceiveSoapMessageAction) super.getAction();
    }
}
