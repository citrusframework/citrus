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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.consol.citrus.ws.SoapAttachment;

/**
 * Simple implementation of SOAP attachment validator. Validating only String attachment contents by simple
 * equals assertion.
 *  
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SimpleSoapAttachmentValidator extends AbstractSoapAttachmentValidator {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleSoapAttachmentValidator.class);
    
    @Override
    protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        if(log.isDebugEnabled()) {
            log.debug("Validating SOAP attachment content ...");
            log.debug("Received attachment content: " + receivedAttachment.getContent().trim());
            log.debug("Control attachment content: " + controlAttachment.getContent().trim());
        }
        
        if(receivedAttachment.getContent() != null) {
            Assert.isTrue(controlAttachment.getContent() != null, 
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + null + "' but was '"
                        + receivedAttachment.getContent().trim() + "'");

            Assert.isTrue(receivedAttachment.getContent().trim().equals(controlAttachment.getContent().trim()),
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + controlAttachment.getContent().trim() + "' but was '"
                        + receivedAttachment.getContent().trim() + "'");
        } else {
            Assert.isTrue(controlAttachment.getContent() == null || controlAttachment.getContent().trim().length() == 0, 
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + controlAttachment.getContent().trim() + "' but was '"
                        + null + "'");
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Validating attachment content: OK");
        }
    }
}
