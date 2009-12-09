/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.actions;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class ReceiveSoapMessageAction extends ReceiveMessageAction {
    private String attachmentData;
    
    private Resource attachmentResource;
    
    private SoapAttachment controlAttachment = new SoapAttachment();
    
    private SoapAttachmentValidator attachmentValidator;
    
    @Override
    protected void validateMessage(Message<?> receivedMessage, TestContext context) {
        super.validateMessage(receivedMessage, context);
        
        try {
            if(StringUtils.hasText(attachmentData)) {
                controlAttachment.setContent(context.replaceDynamicContentInString(attachmentData));
            } else if(attachmentResource != null) {
                controlAttachment.setContent(context.replaceDynamicContentInString(FileUtils.readToString(attachmentResource)));
            }
            
            attachmentValidator.validateAttachment(receivedMessage, controlAttachment);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * @param attachmentData the attachmentData to set
     */
    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
    }

    /**
     * @param attachmentResource the attachmentResource to set
     */
    public void setAttachmentResource(Resource attachmentResource) {
        this.attachmentResource = attachmentResource;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        controlAttachment.setContentType(contentType);
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        controlAttachment.setContentId(contentId);
    }

    /**
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        controlAttachment.setCharsetName(charsetName);
    }

    /**
     * @param attachmentValidator the attachmentValidator to set
     */
    public void setAttachmentValidator(SoapAttachmentValidator attachmentValidator) {
        this.attachmentValidator = attachmentValidator;
    }
}
