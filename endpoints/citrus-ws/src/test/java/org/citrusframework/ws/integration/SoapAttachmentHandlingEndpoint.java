/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ws.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;

import java.util.Iterator;

/**
 * @author Christoph Deppisch
 */
public class SoapAttachmentHandlingEndpoint implements MessageEndpoint {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(SoapAttachmentHandlingEndpoint.class);
    
    public void invoke(MessageContext messageContext) throws Exception {
	    Iterator<Attachment> it = ((SoapMessage)messageContext.getRequest()).getAttachments();
	    while(it.hasNext()) {
	        Attachment attachment = it.next();
	        logger.info("Endpoint handling SOAP attachment: " + attachment.getContentId() + "('" + attachment.getContentType() + "')");
	    }
	}
}
