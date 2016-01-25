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

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.validation.SimpleSoapAttachmentValidator;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Message receiver for SOAP messaging.
 * 
 * This special implementation offers SOAP attachment validation in addition to
 * the normal message receiver.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageAction extends ReceiveMessageAction {
    /** Control attachment */
    private List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();
    
    /** SOAP attachment validator */
    private SoapAttachmentValidator attachmentValidator = new SimpleSoapAttachmentValidator();

    /**
     * Default constructor.
     */
    public ReceiveSoapMessageAction() {
        setName("receive");
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
     * Sets the control attachments.
     * @param attachments the control attachments
     */
    public ReceiveSoapMessageAction setAttachments(List<SoapAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Set the attachment validator.
     * @param attachmentValidator the attachmentValidator to set
     */
    public ReceiveSoapMessageAction setAttachmentValidator(SoapAttachmentValidator attachmentValidator) {
        this.attachmentValidator = attachmentValidator;
        return this;
    }

    /**
     * Gets the attachmentValidator.
     * @return the attachmentValidator
     */
    public SoapAttachmentValidator getAttachmentValidator() {
        return attachmentValidator;
    }
}
