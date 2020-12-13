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
import com.consol.citrus.message.builder.MessageBuilderSupport;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.StaticMessageBuilder;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import com.consol.citrus.ws.message.SoapMessageUtils;
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
    protected void validateMessage(Message message, TestContext context) {
        super.validateMessage(message, context);

        if (!attachments.isEmpty() && !(message instanceof SoapMessage)) {
            throw new CitrusRuntimeException(String.format("Unable to perform SOAP attachment validation on message type '%s'", message.getClass()));
        }

        for (SoapAttachment attachment : attachments) {
            attachment.setTestContext(context);
        }

        if (!attachments.isEmpty()) {
            attachmentValidator.validateAttachment((SoapMessage) message, attachments);
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
    public static final class Builder extends ReceiveMessageActionBuilder<ReceiveSoapMessageAction, Builder.SoapMessageBuilderSupport, Builder> {

        /** Soap message to send or receive */
        private final SoapMessage soapMessage = new SoapMessage();

        private final List<SoapAttachment> attachments = new ArrayList<>();
        private SoapAttachmentValidator attachmentValidator = new SimpleSoapAttachmentValidator();

        public Builder() {
            message(new StaticMessageBuilder(soapMessage))
                    .headerNameIgnoreCase(true);
        }

        @Override
        public SoapMessageBuilderSupport getMessageBuilderSupport() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = new SoapMessageBuilderSupport(soapMessage, this);
            }
            return super.getMessageBuilderSupport();
        }

        public static class SoapMessageBuilderSupport extends MessageBuilderSupport<ReceiveSoapMessageAction, Builder, SoapMessageBuilderSupport> {

            private final SoapMessage soapMessage;

            protected SoapMessageBuilderSupport(SoapMessage soapMessage, Builder delegate) {
                super(delegate);
                this.soapMessage = soapMessage;
            }

            @Override
            public SoapMessageBuilderSupport body(String payload) {
                soapMessage.setPayload(payload);
                return this;
            }

            @Override
            public SoapMessageBuilderSupport name(String name) {
                soapMessage.setName(name);
                return super.name(name);
            }

            @Override
            public SoapMessageBuilderSupport from(Message controlMessage) {
                SoapMessageUtils.copy(controlMessage, soapMessage);
                type(controlMessage.getType());
                return this;
            }

            /**
             * Sets special SOAP action message header.
             * @param soapAction
             * @return
             */
            public SoapMessageBuilderSupport soapAction(String soapAction) {
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
            public SoapMessageBuilderSupport attachment(String contentId, String contentType, String content) {
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
            public SoapMessageBuilderSupport attachment(String contentId, String contentType, Resource contentResource) {
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
            public SoapMessageBuilderSupport attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
                SoapAttachment attachment = new SoapAttachment();
                attachment.setContentId(contentId);
                attachment.setContentType(contentType);
                attachment.setCharsetName(charset.name());

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
            public SoapMessageBuilderSupport charset(String charsetName) {
                if (!delegate.attachments.isEmpty()) {
                    delegate.attachments.get(delegate.attachments.size() - 1).setCharsetName(charsetName);
                }
                return this;
            }

            /**
             * Sets the control attachment from Java object instance.
             * @param attachment
             * @return
             */
            public SoapMessageBuilderSupport attachment(SoapAttachment attachment) {
                delegate.attachments.add(attachment);
                return this;
            }

            /**
             * Set explicit SOAP attachment validator.
             * @param validator
             * @return
             */
            public SoapMessageBuilderSupport attachmentValidator(SoapAttachmentValidator validator) {
                delegate.attachmentValidator = validator;
                return this;
            }

            /**
             * Sets the request content type header.
             * @param contentType
             * @return
             */
            public SoapMessageBuilderSupport contentType(String contentType) {
                soapMessage.header(SoapMessageHeaders.HTTP_CONTENT_TYPE, contentType);
                return this;
            }

            /**
             * Sets the request accept header.
             * @param accept
             * @return
             */
            public SoapMessageBuilderSupport accept(String accept) {
                soapMessage.header(SoapMessageHeaders.HTTP_ACCEPT, accept);
                return this;
            }

            /**
             * Sets the response status.
             * @param status
             * @return
             */
            public SoapMessageBuilderSupport status(HttpStatus status) {
                soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, status.value());
                return this;
            }

            /**
             * Sets the response status code.
             * @param statusCode
             * @return
             */
            public SoapMessageBuilderSupport statusCode(Integer statusCode) {
                soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
                return this;
            }

            /**
             * Sets the context path.
             * @param contextPath
             * @return
             */
            public SoapMessageBuilderSupport contextPath(String contextPath) {
                soapMessage.header(SoapMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
                return this;
            }
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {
            return new ReceiveSoapMessageAction(this);
        }
    }
}
