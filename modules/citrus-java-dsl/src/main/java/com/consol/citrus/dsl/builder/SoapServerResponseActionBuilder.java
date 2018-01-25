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
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapServerResponseActionBuilder extends SendMessageBuilder<SendSoapMessageAction, SoapServerResponseActionBuilder> {

    /** Soap message to send or receive */
    private SoapMessage soapMessage = new SoapMessage();

    /**
     * Default constructor using soap client endpoint.
     * @param delegate
     * @param soapServer
     */
    public SoapServerResponseActionBuilder(DelegatingTestAction<TestAction> delegate, Endpoint soapServer) {
        super(delegate);
        delegate.setDelegate(new SendSoapMessageAction());
        getAction().setEndpoint(soapServer);
        message(soapMessage);
    }

    @Override
    protected void setPayload(String payload) {
        soapMessage.setPayload(payload);
    }

    /**
     * Sets the attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SoapServerResponseActionBuilder attachment(String contentId, String contentType, String content) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        attachment.setContent(content);

        getAction().getAttachments().add(attachment);
        return this;
    }

    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public SoapServerResponseActionBuilder attachment(String contentId, String contentType, Resource contentResource) {
        return attachment(contentId, contentType, contentResource, FileUtils.getDefaultCharset());
    }

    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @param charset
     * @return
     */
    public SoapServerResponseActionBuilder attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);

        try {
            attachment.setContent(FileUtils.readToString(contentResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment resource", e);
        }

        getAction().getAttachments().add(attachment);

        return this;
    }

    /**
     * Sets the charset name for this send action builder's attachment.
     * @param charsetName
     * @return
     */
    public SoapServerResponseActionBuilder charset(String charsetName) {
        if (!getAction().getAttachments().isEmpty()) {
            getAction().getAttachments().get(getAction().getAttachments().size() - 1).setCharsetName(charsetName);
        }
        return this;
    }

    /**
     * Sets the attachment from Java object instance.
     * @param attachment
     * @return
     */
    public SoapServerResponseActionBuilder attachment(SoapAttachment attachment) {
        getAction().getAttachments().add(attachment);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public SoapServerResponseActionBuilder status(HttpStatus status) {
        soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, status.value());
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public SoapServerResponseActionBuilder statusCode(Integer statusCode) {
        soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
        return this;
    }

    /**
     * Sets the response content type header.
     * @param contentType
     * @return
     */
    public SoapServerResponseActionBuilder contentType(String contentType) {
        soapMessage.header(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
        return this;
    }
    
    public SoapServerResponseActionBuilder mtomEnabled(boolean mtomEnabled) {
        soapMessage.mtomEnabled(mtomEnabled);
        getAction().setMtomEnabled(mtomEnabled);
        return this;
    }

    @Override
    protected SendSoapMessageAction getAction() {
        return (SendSoapMessageAction) super.getAction();
    }
}
