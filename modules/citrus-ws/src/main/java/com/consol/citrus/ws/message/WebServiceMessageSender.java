/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.message;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.message.callback.*;
/**
 * Message sender connection as client to a WebService endpoint. The sender supports
 * SOAP attachments in contrary to the normal message senders.
 * 
 * @author Christoph Deppisch
 */
public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender, FaultMessageResolver {

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /** Resolves dynamic endpoint uri */
    private EndpointUriResolver endpointResolver;
    
    /** WS adressing specific headers */
    private WsAddressingHeaders addressingHeaders;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WebServiceMessageSender.class);
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        send(message, null);
    }
    
    /**
     * Send message with SOAP attachment.
     * @param message
     * @param attachment
     */
    public void send(final Message<?> message, final Attachment attachment) {
        Assert.notNull(message, "Message is empty - unable to send empty message");
        
        String endpointUri;
        if (endpointResolver != null) {
            endpointUri = endpointResolver.resolveEndpointUri(message, getDefaultUri());
        } else { // use default uri
            endpointUri = getDefaultUri();
        }
        
        log.info("Sending SOAP message to endpoint: '" + endpointUri + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + message.toString());
        }
        
        if(!(message.getPayload() instanceof String)) {
        	throw new CitrusRuntimeException("Unsupported payload type '" + message.getPayload().getClass() +
    				"' Currently only 'java.lang.String' is supported as payload type.");
        }
        
        WebServiceMessageCallback requestCallback;
        if (addressingHeaders == null) {
            requestCallback = new SoapRequestMessageCallback(message, attachment);
        } else {
            requestCallback = new WsAddressingRequestMessageCallback(message, 
                    attachment, addressingHeaders);
        }
        
        SoapResponseMessageCallback responseCallback = new SoapResponseMessageCallback();
        
        getWebServiceTemplate().setFaultMessageResolver(this);
        
        // send and receive message
        if (endpointResolver != null) {
            getWebServiceTemplate().sendAndReceive(endpointUri, requestCallback, responseCallback);
        } else { // use default endpoint uri
            getWebServiceTemplate().sendAndReceive(requestCallback, responseCallback);
        }

        log.info("SOAP message was successfully sent to endpoint: '" + endpointUri + "'");
        
        Message<String> responseMessage = responseCallback.getResponse();
        
        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }
    
    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
    
    /**
     * @see org.springframework.ws.client.core.FaultMessageResolver#resolveFault(org.springframework.ws.WebServiceMessage)
     */
	public void resolveFault(WebServiceMessage message) throws IOException {
		if(message instanceof SoapMessage) {
			new SoapFaultMessageResolver().resolveFault(message);
		} else {
			new SimpleFaultMessageResolver().resolveFault(message);
		}
	}

    /**
     * Set reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
    
    /**
     * Sets the endpoint uri resolver.
     * @param endpointResolver the endpointUriResolver to set
     */
    public void setEndpointResolver(EndpointUriResolver endpointResolver) {
        this.endpointResolver = endpointResolver;
    }

    /**
     * Sets the ws addressing headers for this message sender.
     * @param addressingHeaders the addressingHeaders to set
     */
    public void setAddressingHeaders(WsAddressingHeaders addressingHeaders) {
        this.addressingHeaders = addressingHeaders;
    }
}
