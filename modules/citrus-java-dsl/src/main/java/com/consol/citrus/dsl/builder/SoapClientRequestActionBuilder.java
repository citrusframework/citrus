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
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.*;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapClientRequestActionBuilder extends SendMessageBuilder<SendSoapMessageAction, SoapClientRequestActionBuilder> {

    /** Soap message to send or receive */
    private SoapMessage soapMessage = new SoapMessage();

    /**
     * Default constructor using soap client endpoint.
     * @param delegate
     * @param soapClient
     */
    public SoapClientRequestActionBuilder(DelegatingTestAction<TestAction> delegate, Endpoint soapClient) {
        super(delegate);
        delegate.setDelegate(new SendSoapMessageAction());
        getAction().setEndpoint(soapClient);
        getAction().setMessageBuilder(new StaticMessageContentBuilder(soapMessage));
    }

    /**
     * Default constructor using soap client uri.
     * @param delegate
     * @param soapClientUri
     */
    public SoapClientRequestActionBuilder(DelegatingTestAction<TestAction> delegate, String soapClientUri) {
        super(delegate);
        delegate.setDelegate(new SendSoapMessageAction());
        getAction().setEndpointUri(soapClientUri);
        getAction().setMessageBuilder(new StaticMessageContentBuilder(soapMessage));
    }

    @Override
    protected void setPayload(String payload) {
        soapMessage.setPayload(payload);
    }

    /**
     * Sets special SOAP action message header.
     * @param soapAction
     * @return
     */
    public SoapClientRequestActionBuilder soapAction(String soapAction) {
        soapMessage.header(SoapMessageHeaders.SOAP_ACTION, soapAction);
        return this;
    }

    /**
     * Sets the attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SoapClientRequestActionBuilder attachment(String contentId, String contentType, String content) {
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
    public SoapClientRequestActionBuilder attachment(String contentId, String contentType, Resource contentResource) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);

        try {
            attachment.setContent(FileUtils.readToString(contentResource));
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
    public SoapClientRequestActionBuilder charset(String charsetName) {
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
    public SoapClientRequestActionBuilder attachment(SoapAttachment attachment) {
        getAction().getAttachments().add(attachment);
        return this;
    }

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    public SoapClientRequestActionBuilder uri(String uri) {
        soapMessage.header(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, uri);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public SoapClientRequestActionBuilder contentType(String contentType) {
        soapMessage.header(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public SoapClientRequestActionBuilder accept(String accept) {
        soapMessage.header(SoapMessageHeaders.HTTP_ACCEPT, accept);
        return this;
    }

    @Override
    protected SendSoapMessageAction getAction() {
        return (SendSoapMessageAction) super.getAction();
    }
}
