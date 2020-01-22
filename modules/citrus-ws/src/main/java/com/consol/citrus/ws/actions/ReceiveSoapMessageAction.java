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

package com.consol.citrus.ws.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import com.consol.citrus.ws.validation.SimpleSoapAttachmentValidator;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

/**
 * Message receiver for SOAP messaging.
 *
 * This special implementation offers SOAP attachment validation in addition to
 * the normal message receiver.
 *
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageAction extends ReceiveMessageAction implements TestAction {
    /** Control attachment */
    private final List<SoapAttachment> attachments;

    /** SOAP attachment validator */
    private final SoapAttachmentValidator attachmentValidator;

    /**
     * Default constructor.
     */
    public ReceiveSoapMessageAction(Builder builder) {
        super(builder);

        this.attachments = builder.attachments;
        this.attachmentValidator = builder.attachmentValidator;
    }

    @Override
    protected void validateMessage(Message receivedMessage, TestContext context) {
        try {
            super.validateMessage(receivedMessage, context);

            if (!attachments.isEmpty() && !(receivedMessage instanceof SoapMessage)) {
                throw new CitrusRuntimeException(String.format("Unable to perform SOAP attachment validation on message type '%s'", receivedMessage.getClass()));
            }

            for (SoapAttachment attachment : attachments) {
                attachment.setTestContext(context);
            }

            if (!attachments.isEmpty()) {
                attachmentValidator.validateAttachment((SoapMessage) receivedMessage, attachments);
            }

        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Gets the control attachments.
     * @return the control attachments
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Gets the attachmentValidator.
     * @return the attachmentValidator
     */
    public SoapAttachmentValidator getAttachmentValidator() {
        return attachmentValidator;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends ReceiveMessageActionBuilder<ReceiveSoapMessageAction, Builder> {

        private List<SoapAttachment> attachments = new ArrayList<>();
        private SoapAttachmentValidator attachmentValidator = new SimpleSoapAttachmentValidator();

        /** Soap message to send or receive */
        protected SoapMessage soapMessage = new SoapMessage();

        public Builder() {
            messageBuilder(new StaticMessageContentBuilder(soapMessage));
            messageType(MessageType.XML);
            headerNameIgnoreCase(true);
        }

        /**
         * Sets special SOAP action message header.
         * @param soapAction
         * @return
         */
        public Builder soapAction(String soapAction) {
            soapMessage.header(SoapMessageHeaders.SOAP_ACTION, soapAction);
            return this;
        }

        /**
         * Sets the control attachment with string content.
         * @param contentId
         * @param contentType
         * @param content
         * @return
         */
        public Builder attachment(String contentId, String contentType, String content) {
            SoapAttachment attachment = new SoapAttachment();
            attachment.setContentId(contentId);
            attachment.setContentType(contentType);
            attachment.setContent(content);

            attachment(attachment);

            return this;
        }

        /**
         * Sets the control attachment with content resource.
         * @param contentId
         * @param contentType
         * @param contentResource
         * @return
         */
        public Builder attachment(String contentId, String contentType, Resource contentResource) {
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
        public Builder attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
            SoapAttachment attachment = new SoapAttachment();
            attachment.setContentId(contentId);
            attachment.setContentType(contentType);

            try {
                attachment.setContent(FileUtils.readToString(contentResource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read attachment content resource", e);
            }

            attachment(attachment);

            return this;
        }

        /**
         * Sets the charset name for this send action builder's control attachment.
         * @param charsetName
         * @return
         */
        public Builder charset(String charsetName) {
            if (!this.attachments.isEmpty()) {
                this.attachments.get(this.attachments.size() - 1).setCharsetName(charsetName);
            }
            return this;
        }

        /**
         * Sets the control attachment from Java object instance.
         * @param attachment
         * @return
         */
        public Builder attachment(SoapAttachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        /**
         * Set explicit SOAP attachment validator.
         * @param validator
         * @return
         */
        public Builder attachmentValidator(SoapAttachmentValidator validator) {
            this.attachmentValidator = validator;
            return this;
        }

        /**
         * Sets the request content type header.
         * @param contentType
         * @return
         */
        public Builder contentType(String contentType) {
            soapMessage.header(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
            return this;
        }

        /**
         * Sets the request accept header.
         * @param accept
         * @return
         */
        public Builder accept(String accept) {
            soapMessage.header(SoapMessageHeaders.HTTP_ACCEPT, accept);
            return this;
        }

        /**
         * Sets the response status.
         * @param status
         * @return
         */
        public Builder status(HttpStatus status) {
            soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, status.value());
            return this;
        }

        /**
         * Sets the response status code.
         * @param statusCode
         * @return
         */
        public Builder statusCode(Integer statusCode) {
            soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
            return this;
        }

        /**
         * Sets the context path.
         * @param contextPath
         * @return
         */
        public Builder contextPath(String contextPath) {
            soapMessage.header(SoapMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
            return this;
        }

        @Override
        public ReceiveSoapMessageAction build() {
            return new ReceiveSoapMessageAction(this);
        }
    }
}
