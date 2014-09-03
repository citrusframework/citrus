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
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.validation.SimpleSoapAttachmentValidator;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.messaging.Message;

import java.io.IOException;

/**
 * Message receiver for SOAP messaging.
 * 
 * This special implementation offers SOAP attachment validation in addition to
 * the normal message receiver.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageAction extends ReceiveMessageAction {
    /** Attachment body content data */
    private String attachmentData;
    
    /** Attachment body content in external file resource path */
    private String attachmentResourcePath;
    
    /** Control attachment */
    private SoapAttachment controlAttachment = new SoapAttachment();
    
    /** SOAP attachment validator */
    private SoapAttachmentValidator attachmentValidator = new SimpleSoapAttachmentValidator();

    /**
     * Default constructor.
     */
    public ReceiveSoapMessageAction() {
        setName("receive");
    }

    /**
     * @see com.consol.citrus.actions.ReceiveMessageAction#validateMessage(org.springframework.messaging.Message, com.consol.citrus.context.TestContext)
     */
    @Override
    protected void validateMessage(Message<?> receivedMessage, TestContext context) {
        try {
            super.validateMessage(receivedMessage, context);
            
            if (attachmentData != null) {
                controlAttachment.setContent(context.replaceDynamicContentInString(attachmentData));
            } else if (attachmentResourcePath != null) {
                controlAttachment.setContent(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(attachmentResourcePath, context))));
            } else {
                return; //no attachment expected, no validation
            }
            
            // handle variables in content id
            if (controlAttachment.getContentId() != null) {
            	controlAttachment.setContentId(context.replaceDynamicContentInString(controlAttachment.getContentId()));
            }
            
            // handle variables in content type
            if (controlAttachment.getContentType() != null) {
                controlAttachment.setContentType(context.replaceDynamicContentInString(controlAttachment.getContentType()));
            }
            
            attachmentValidator.validateAttachment(receivedMessage, controlAttachment);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the attachment data as string value.
     * @param attachmentData the attachmentData to set
     */
    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
    }

    /**
     * Set the attachment data from external file resource. 
     * @param attachmentResource the attachmentResource to set
     */
    public void setAttachmentResourcePath(String attachmentResource) {
        this.attachmentResourcePath = attachmentResource;
    }

    /**
     * Set the content type, delegates to control attachment.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        controlAttachment.setContentType(contentType);
    }

    /**
     * Set the content id, delegates to control attachment.
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        controlAttachment.setContentId(contentId);
    }

    /**
     * Set the charset name, delegates to control attachment.
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        controlAttachment.setCharsetName(charsetName);
    }

    /**
     * Set the attachment validator.
     * @param attachmentValidator the attachmentValidator to set
     */
    public void setAttachmentValidator(SoapAttachmentValidator attachmentValidator) {
        this.attachmentValidator = attachmentValidator;
    }

    /**
     * Gets the controlAttachment.
     * @return the controlAttachment
     */
    public SoapAttachment getControlAttachment() {
        return controlAttachment;
    }

    /**
     * Gets the attachmentData.
     * @return the attachmentData
     */
    public String getAttachmentData() {
        return attachmentData;
    }

    /**
     * Gets the attachmentResource.
     * @return the attachmentResource
     */
    public String getAttachmentResourcePath() {
        return attachmentResourcePath;
    }

    /**
     * Gets the attachmentValidator.
     * @return the attachmentValidator
     */
    public SoapAttachmentValidator getAttachmentValidator() {
        return attachmentValidator;
    }
}
