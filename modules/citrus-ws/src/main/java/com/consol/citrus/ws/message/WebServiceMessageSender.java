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

package com.consol.citrus.ws.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender, FaultMessageResolver {

    private ReplyMessageHandler replyMessageHandler;
    
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceMessageSender.class);
    
    public void send(Message<?> message) {
        send(message, null);
    }
    
    public void send(final Message<?> message, final Attachment attachment) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDefaultUri());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        if(message.getPayload() instanceof String == false) {
        	throw new CitrusRuntimeException("Unsupported payload type '" + message.getPayload().getClass() +
    				"' Currently only 'java.lang.String' is supported as payload type.");
        }
        
        StringResult result = new StringResult();

        getWebServiceTemplate().setFaultMessageResolver(this);
        
        getWebServiceTemplate().sendSourceAndReceiveToResult(new StringSource((String)message.getPayload()), 
                new WebServiceMessageCallback() {
                    public void doWithMessage(WebServiceMessage requestMessage)
                            throws IOException, TransformerException {
                        SoapMessage soapRequest = ((SoapMessage)requestMessage);
                        
                        for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
                            if(headerEntry.getKey().startsWith(MessageHeaders.PREFIX)) {
                                continue;
                            }
                            
                            if(headerEntry.getKey().toLowerCase().endsWith("soapaction")) {
                                soapRequest.setSoapAction(headerEntry.getValue().toString());
                            } else {
                                SoapHeaderElement headerElement;
                                if(QNameUtils.validateQName(headerEntry.getKey())) {
                                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
                                } else {
                                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.createQName("", headerEntry.getKey(), ""));
                                }
                                
                                headerElement.setText(headerEntry.getValue().toString());
                            }
                        }
                        
                        if(attachment != null) {
                            if(log.isDebugEnabled()) {
                                log.debug("Adding attachment to SOAP message: '" + attachment.getContentId() + "' ('" + attachment.getContentType() + "')");
                            }
                            
                            soapRequest.addAttachment(attachment.getContentId(), new InputStreamSource() {
                                public InputStream getInputStream() throws IOException {
                                    return attachment.getInputStream();
                                }
                            }, attachment.getContentType());
                        }
                    }
        }, result);

        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(result.toString()).build(),
                        correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(result.toString()).build());
            }
        }
    }

    /**
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
    
	public void resolveFault(WebServiceMessage message) throws IOException {
		if(message instanceof SoapMessage) {
			new SoapFaultMessageResolver().resolveFault(message);
		} else {
			new SimpleFaultMessageResolver().resolveFault(message);
		}
	}

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
