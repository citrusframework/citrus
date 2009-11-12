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

package com.consol.citrus.ws.validation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.util.Assert;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.SoapAttachmentHeaders;

/**
 * Abstract validator tries to find attachment in received message through its attachment contentId, contentType
 * and content as String value. Validator will create a com.consol.citrus.ws.SoapAttachment and automatically handle contentId and 
 * contentType validation. Content validation is delegated to subclass.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
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
            
            if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT_ID)) {
                attachment.setContentId(receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT_ID).toString());
            }
            
            if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT_TYPE)) {
                attachment.setContentType(receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT_TYPE).toString());
            }
            
            if(receivedMessage.getHeaders().containsKey(SoapAttachmentHeaders.CONTENT)) {
                attachment.setContent(receivedMessage.getHeaders().get(SoapAttachmentHeaders.CONTENT).toString());
            }
            
            if(attachment.getContentId() != null) {
                Assert.isTrue(controlAttachment.getContentId() != null, 
                        "Values not equal for attachment contentId, expected '"
                            + null + "' but was '"
                            + attachment.getContentId() + "'");

                Assert.isTrue(attachment.getContentId().equals(controlAttachment.getContentId()),
                        "Values not equal for attachment contentId, expected '"
                            + controlAttachment.getContentId() + "' but was '"
                            + attachment.getContentId() + "'");
            } else {
                Assert.isTrue(controlAttachment.getContentId() == null || controlAttachment.getContentId().length() == 0, 
                        "Values not equal for attachment contentId, expected '"
                            + controlAttachment.getContentId() + "' but was '"
                            + null + "'");
            }
            
            if(log.isDebugEnabled()) {
                log.debug("Validating attachment contentId: " + attachment.getContentId() + 
                        "='" + controlAttachment.getContentId() + "': OK.");
            }
            
            if(attachment.getContentType() != null) {
                Assert.isTrue(controlAttachment.getContentType() != null, 
                        "Values not equal for attachment contentType, expected '"
                            + null + "' but was '"
                            + attachment.getContentType() + "'");

                Assert.isTrue(attachment.getContentType().equals(controlAttachment.getContentType()),
                        "Values not equal for attachment contentType, expected '"
                            + controlAttachment.getContentType() + "' but was '"
                            + attachment.getContentType() + "'");
            } else {
                Assert.isTrue(controlAttachment.getContentType() == null || controlAttachment.getContentType().length() == 0, 
                        "Values not equal for attachment contentType, expected '"
                            + controlAttachment.getContentType() + "' but was '"
                            + null + "'");
            }
            
            if(log.isDebugEnabled()) {
                log.debug("Validating attachment contentType: " + attachment.getContentType() + 
                        "='" + controlAttachment.getContentType() + "': OK.");
            }
            
            validateAttachmentContent(attachment, controlAttachment);
            
            log.info("Validation of SOAP attachment finished successfully: All values OK");
        } else {
            throw new CitrusRuntimeException("Missing SOAP attachment with contentId '" + controlAttachment.getContentId() + "'");
        }
    }

    /**
     * @param attachment
     * @param controlAttachment
     */
    protected abstract void validateAttachmentContent(SoapAttachment receivedAttContent, SoapAttachment controlAttachment);
}
