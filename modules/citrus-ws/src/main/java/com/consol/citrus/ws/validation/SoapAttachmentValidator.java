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

import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;

import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.WebServiceEndpoint;


/**
 * Interface for SOAP attachment validators. 
 * 
 * The Citrus {@link WebServiceEndpoint} implementation adds the received SOAP attachments as 
 * {@link Attachment} implementations to the Spring integration message header. The header name will be the
 * attachment's contentId. The header value is the {@link Attachment} object.
 *  
 * @author Christoph Deppisch
 */
public interface SoapAttachmentValidator {

    /**
     * Validate the attachment with a given control attachment.
     * 
     * @param receivedMessage
     * @param controlAttachment
     * @throws IOException
     */
    public void validateAttachment(Message<?> receivedMessage, SoapAttachment controlAttachment) throws IOException;
}
