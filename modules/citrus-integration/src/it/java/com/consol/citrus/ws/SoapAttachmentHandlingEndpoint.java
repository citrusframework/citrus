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
