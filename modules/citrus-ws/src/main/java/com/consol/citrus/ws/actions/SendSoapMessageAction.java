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

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Message sender implementation sending SOAP messages.
 * 
 *  This special implementation adds SOAP attachment support to normal
 *  message sender.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction {

    /** SOAP attachment data */
    private String attachmentData;
    
    /** SOAP attachment data as external file resource path */
    private String attachmentResourcePath;
    
    /** SOAP attachment */
    private SoapAttachment attachment = new SoapAttachment();

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        Message message = super.createMessage(context, getMessageType());

        final SoapMessage soapMessage;
        final String attachmentContent;

        try {
            if (StringUtils.hasText(attachmentData)) {
                attachmentContent = context.replaceDynamicContentInString(attachmentData);
            } else if (attachmentResourcePath != null) {
                attachmentContent = context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(attachmentResourcePath, context)));
            } else {
                attachmentContent = null;
            }

            soapMessage = new SoapMessage(message);

            if (attachmentContent != null) {
                attachment.setContent(attachmentContent);
                soapMessage.addAttachment(attachment);
            }

        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        return soapMessage;
    }

    /**
     * Set the Attachment data file resource.
     * @param attachment the attachment to set
     */
    public void setAttachmentResourcePath(String attachment) {
        this.attachmentResourcePath = attachment;
    }

    /**
     * Set the content type, delegates to soap attachment.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        attachment.setContentType(contentType);
    }

    /**
     * Set the content id, delegates to soap attachment.
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        attachment.setContentId(contentId);
    }
    
    /**
     * Set the charset name, delegates to soap attachment.
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        attachment.setCharsetName(charsetName);
    }

    /**
     * Set the attachment data as string value.
     * @param attachmentData the attachmentData to set
     */
    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
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
     * Gets the attachment.
     * @return the attachment
     */
    public SoapAttachment getAttachment() {
        return attachment;
    }
}
