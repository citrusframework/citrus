/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

package com.consol.citrus.ws;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;

public class SoapAttachmentHandlingEndpoint implements MessageEndpoint {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SoapAttachmentHandlingEndpoint.class);
    
	@SuppressWarnings("unchecked")
    public void invoke(MessageContext messageContext) throws Exception {
	    Iterator<Attachment> it = ((SoapMessage)messageContext.getRequest()).getAttachments();
	    while(it.hasNext()) {
	        Attachment attachment = it.next();
	        log.info("Endpoint handling SOAP attachment: " + attachment.getContentId() + "('" + attachment.getContentType() + "')");
	    }
	}
}
