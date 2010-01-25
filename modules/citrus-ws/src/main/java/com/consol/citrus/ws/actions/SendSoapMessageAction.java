/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.actions;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.WebServiceMessageSender;

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
    
    /** SOAP attachment data as external file resource */
    private Resource attachmentResource;
    
    /** SOAP attachment */
    private SoapAttachment attachment = new SoapAttachment();
    
    /**
     * @see com.consol.citrus.actions.SendMessageAction#execute(com.consol.citrus.context.TestContext)
     */
    @Override
    public void execute(final TestContext context) {
        Message<?> message = createMessage(context);
        
        context.createVariablesFromHeaderValues(extractHeaderValues, message.getHeaders());
        
        if(!(messageSender instanceof WebServiceMessageSender)) {
            throw new CitrusRuntimeException("Sending SOAP messages requires a " +
            		"'com.consol.citrus.ws.message.WebServiceMessageSender' but was '" + message.getClass().getName() + "'");
        }
        
        String content = null;
        try {
            if(StringUtils.hasText(attachmentData)) {
                content = context.replaceDynamicContentInString(attachmentData);
            } else if(attachmentResource != null) {
                content = context.replaceDynamicContentInString(FileUtils.readToString(attachmentResource));
            }
        
            if(content != null) {
                attachment.setContent(content);
                ((WebServiceMessageSender)messageSender).send(message, attachment);
            } else {
                ((WebServiceMessageSender)messageSender).send(message);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Set the Attachment data file resource.
     * @param attachment the attachment to set
     */
    public void setAttachmentResource(Resource attachment) {
        this.attachmentResource = attachment;
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
}
