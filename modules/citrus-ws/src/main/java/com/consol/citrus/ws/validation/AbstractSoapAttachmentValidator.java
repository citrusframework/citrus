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

package com.consol.citrus.ws.validation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.SoapAttachmentHeaders;

/**
 * Abstract validator tries to find attachment in received message through its attachment contentId, contentType
 * and content as String value. Validator will create a com.consol.citrus.ws.SoapAttachment and automatically handle contentId and 
 * contentType validation. Content validation is delegated to subclass.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractSoapAttachmentValidator implements SoapAttachmentValidator {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractSoapAttachmentValidator.class);
    
    /**
     * @param receivedMessage
     * @param controlAttachment
     */
    public void validateAttachment(Message<?> receivedMessage, SoapAttachment controlAttachment) throws IOException {
        log.info("Validating SOAP attachments ...");
        
        if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT_ID)) {
            if(log.isDebugEnabled()) {
                log.debug("Found attachment with contentId '" + receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT_ID) + "'");
            }
            
            SoapAttachment attachment = new SoapAttachment();
            
            attachment.setContentId(receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT_ID).toString());
            
            if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT_TYPE)) {
                attachment.setContentType(receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT_TYPE).toString());
            }
            
            if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT)) {
                Object contentObject = receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT);
                
                if(contentObject instanceof byte[]) {
                    String content = new String((byte[])contentObject, controlAttachment.getCharsetName());
                    
                    if(content.contains("<?xml")) {
                        //strip off possible leading prolog characters in xml content
                        attachment.setContent(content.substring(content.indexOf("<")));
                    } else {
                        attachment.setContent(content);
                    }
                } else if(contentObject instanceof String) {
                    attachment.setContent(contentObject.toString());
                } else {
                    throw new IllegalArgumentException("Unsupported attachment content object (" + contentObject.getClass() + ")." +
                    		" Either byte[] or java.lang.String are supported.");
                }
            }
            
            validateAttachmentContentId(attachment, controlAttachment);
            validateAttachmentContentType(attachment, controlAttachment);
            validateAttachmentContent(attachment, controlAttachment);
            
            log.info("Validation of SOAP attachment finished successfully: All values OK");
        } else {
            throw new CitrusRuntimeException("Missing SOAP attachment with contentId '" + controlAttachment.getContentId() + "'");
        }
    }
    
    protected void validateAttachmentContentId(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        //in case contentId was not set in test case, skip validation 
        if(!StringUtils.hasText(controlAttachment.getContentId())) { return; }
        
        if(receivedAttachment.getContentId() != null) {
            Assert.isTrue(controlAttachment.getContentId() != null, 
                    "Values not equal for attachment contentId, expected '"
                        + null + "' but was '"
                        + receivedAttachment.getContentId() + "'");

            Assert.isTrue(receivedAttachment.getContentId().equals(controlAttachment.getContentId()),
                    "Values not equal for attachment contentId, expected '"
                        + controlAttachment.getContentId() + "' but was '"
                        + receivedAttachment.getContentId() + "'");
        } else {
            Assert.isTrue(controlAttachment.getContentId() == null || controlAttachment.getContentId().length() == 0, 
                    "Values not equal for attachment contentId, expected '"
                        + controlAttachment.getContentId() + "' but was '"
                        + null + "'");
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Validating attachment contentId: " + receivedAttachment.getContentId() + 
                    "='" + controlAttachment.getContentId() + "': OK.");
        }
    }
    
    protected void validateAttachmentContentType(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        //in case contentType was not set in test case, skip validation
        if(!StringUtils.hasText(controlAttachment.getContentType())) { return; }
        
        if(receivedAttachment.getContentType() != null) {
            Assert.isTrue(controlAttachment.getContentType() != null, 
                    "Values not equal for attachment contentType, expected '"
                        + null + "' but was '"
                        + receivedAttachment.getContentType() + "'");

            Assert.isTrue(receivedAttachment.getContentType().equals(controlAttachment.getContentType()),
                    "Values not equal for attachment contentType, expected '"
                        + controlAttachment.getContentType() + "' but was '"
                        + receivedAttachment.getContentType() + "'");
        } else {
            Assert.isTrue(controlAttachment.getContentType() == null || controlAttachment.getContentType().length() == 0, 
                    "Values not equal for attachment contentType, expected '"
                        + controlAttachment.getContentType() + "' but was '"
                        + null + "'");
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Validating attachment contentType: " + receivedAttachment.getContentType() + 
                    "='" + controlAttachment.getContentType() + "': OK.");
        }
    }

    /**
     * @param attachment
     * @param controlAttachment
     */
    protected abstract void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment);
}
