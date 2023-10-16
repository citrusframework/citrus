/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.actions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.citrusframework.TestAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.SendMessageBuilderSupport;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.ws.message.SoapMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message send action able to add SOAP attachment support to normal message sending action.
 *
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction implements TestAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SendSoapMessageAction.class);

    /** SOAP attachments */
    private final List<SoapAttachment> attachments;

    /** enable/disable mtom attachments */
    private final boolean mtomEnabled;

    /** Marker for inline mtom binary data */
    public static final String CID_MARKER = "cid:";

    public SendSoapMessageAction(SendSoapMessageBuilder<?, ?, ?> builder) {
        super(builder);

        this.attachments = builder.getMessageBuilderSupport().getAttachments();
        this.mtomEnabled = builder.getMessageBuilderSupport().isMtomEnabled();
    }

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        Message message = super.createMessage(context, getMessageType());

        SoapMessage soapMessage = new SoapMessage(message).mtomEnabled(mtomEnabled);
        try {
            for (SoapAttachment attachment : attachments) {
                attachment.setTestContext(context);

                if (mtomEnabled) {
                    String messagePayload = soapMessage.getPayload(String.class);
                    String cid = CID_MARKER + attachment.getContentId();

                    if (attachment.isMtomInline() && messagePayload.contains(cid)) {
                        byte[] attachmentBinaryData = FileUtils.readToString(attachment.getInputStream(), Charset.forName(attachment.getCharsetName())).getBytes(Charset.forName(attachment.getCharsetName()));
                        if (attachment.getEncodingType().equals(SoapAttachment.ENCODING_BASE64_BINARY)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("Adding inline base64Binary data for attachment: %s", cid));
                            }
                            messagePayload = messagePayload.replaceAll(cid, Base64.encodeBase64String(attachmentBinaryData));
                        } else if (attachment.getEncodingType().equals(SoapAttachment.ENCODING_HEX_BINARY)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("Adding inline hexBinary data for attachment: %s", cid));
                            }
                            messagePayload = messagePayload.replaceAll(cid, Hex.encodeHexString(attachmentBinaryData).toUpperCase());
                        } else {
                            throw new CitrusRuntimeException(String.format("Unsupported encoding type '%s' for SOAP attachment: %s - choose one of %s or %s",
                                    attachment.getEncodingType(), cid, SoapAttachment.ENCODING_BASE64_BINARY, SoapAttachment.ENCODING_HEX_BINARY));
                        }
                    } else {
                        messagePayload = messagePayload.replaceAll(cid, String.format("<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"%s\"/>", CID_MARKER + URLEncoder.encode(attachment.getContentId(), "UTF-8")));
                        soapMessage.addAttachment(attachment);
                    }

                    soapMessage.setPayload(messagePayload);
                } else {
                    soapMessage.addAttachment(attachment);
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        return soapMessage;
    }

    /**
     * Gets the attachments.
     * @return the attachments
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Gets mtom attachments enabled
     * @return
     */
    public boolean getMtomEnabled() {
        return this.mtomEnabled;
    }

    /**
     * Gets mtom enabled.
     * @return
     */
    public boolean isMtomEnabled() {
        return mtomEnabled;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends SendSoapMessageBuilder<SendSoapMessageAction, Builder.SendSoapMessageBuilderSupport, Builder> {

        public Builder() {
            message(new StaticMessageBuilder(soapMessage));
        }

        @Override
        public SendSoapMessageBuilderSupport getMessageBuilderSupport() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = new SendSoapMessageBuilderSupport(soapMessage, this);
            }
            return super.getMessageBuilderSupport();
        }

        public static class SendSoapMessageBuilderSupport extends SoapMessageBuilderSupport<SendSoapMessageAction, Builder, SendSoapMessageBuilderSupport> {

            protected SendSoapMessageBuilderSupport(SoapMessage soapMessage, Builder delegate) {
                super(soapMessage, delegate);
            }
        }

        @Override
        public SendSoapMessageAction doBuild() {
            return new SendSoapMessageAction(this);
        }
    }

    /**
     * Action builder.
     */
    public abstract static class SendSoapMessageBuilder<T extends SendSoapMessageAction, M extends SoapMessageBuilderSupport<T, B, M>, B extends SendSoapMessageBuilder<T, M, B>> extends SendMessageActionBuilder<T, M, B> {

        /** Soap message to send */
        protected SoapMessage soapMessage = new SoapMessage();

        public B mtomEnabled(boolean mtomEnabled) {
            getMessageBuilderSupport().mtomEnabled(mtomEnabled);
            return self;
        }

    }

    public static class SoapMessageBuilderSupport<T extends SendSoapMessageAction, B extends SendSoapMessageBuilder<T, M, B>, M extends SoapMessageBuilderSupport<T, B, M>> extends SendMessageBuilderSupport<T, B, M> {

        protected final SoapMessage soapMessage;

        private final List<SoapAttachment> attachments = new ArrayList<>();
        private boolean mtomEnabled = false;

        protected SoapMessageBuilderSupport(SoapMessage soapMessage, B delegate) {
            super(delegate);
            this.soapMessage = soapMessage;
        }

        @Override
        public M body(String payload) {
            soapMessage.setPayload(payload);
            return self;
        }

        @Override
        public M name(String name) {
            soapMessage.setName(name);
            return super.name(name);
        }

        @Override
        public M from(Message controlMessage) {
            SoapMessageUtils.copy(controlMessage, soapMessage);
            type(controlMessage.getType());
            return self;
        }

        /**
         * Sets special SOAP action message header.
         * @param soapAction
         * @return
         */
        public M soapAction(String soapAction) {
            soapMessage.header(SoapMessageHeaders.SOAP_ACTION, soapAction);
            return self;
        }

        /**
         * Sets the attachment with string content.
         * @param contentId
         * @param contentType
         * @param content
         * @return
         */
        public M attachment(String contentId, String contentType, String content) {
            SoapAttachment attachment = new SoapAttachment();
            attachment.setContentId(contentId);
            attachment.setContentType(contentType);
            attachment.setContent(content);

            attachment(attachment);
            return self;
        }

        /**
         * Sets the attachment with content resource.
         * @param contentId
         * @param contentType
         * @param contentResource
         * @return
         */
        public M attachment(String contentId, String contentType, Resource contentResource) {
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
        public M attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
            SoapAttachment attachment = new SoapAttachment();
            attachment.setContentId(contentId);
            attachment.setContentType(contentType);

            try {
                attachment.setContent(FileUtils.readToString(contentResource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read attachment resource", e);
            }

            attachment(attachment);

            return self;
        }

        /**
         * Sets the charset name for this send action builder's most recent attachment.
         * @param charsetName
         * @return
         */
        public M charset(String charsetName) {
            if (!this.attachments.isEmpty()) {
                this.attachments.get(this.attachments.size() - 1).setCharsetName(charsetName);
            }
            return self;
        }

        /**
         * Sets the attachment from Java object instance.
         * @param attachment
         * @return
         */
        public M attachment(SoapAttachment attachment) {
            this.attachments.add(attachment);
            return self;
        }

        /**
         * Set the endpoint URI for the request. This works only if the HTTP endpoint used
         * doesn't provide an own endpoint URI resolver.
         *
         * @param uri absolute URI to use for the endpoint
         * @return self
         */
        public M uri(String uri) {
            soapMessage.header(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, uri);
            return self;
        }

        /**
         * Sets the request content type header.
         * @param contentType
         * @return
         */
        public M contentType(String contentType) {
            soapMessage.contentType(contentType);
            return self;
        }

        /**
         * Sets the request accept header.
         * @param accept
         * @return
         */
        public M accept(String accept) {
            soapMessage.accept(accept);
            return self;
        }

        public M mtomEnabled(boolean mtomEnabled) {
            soapMessage.mtomEnabled(mtomEnabled);
            this.mtomEnabled = mtomEnabled;
            return self;
        }

        protected List<SoapAttachment> getAttachments() {
            return attachments;
        }

        protected boolean isMtomEnabled() {
            return mtomEnabled;
        }
    }
}
