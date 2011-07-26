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

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.xml.transform.StringResult;

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
public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender {

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /** Resolves dynamic endpoint uri */
    private EndpointUriResolver endpointResolver;
    
    /** WS adressing specific headers */
    private WsAddressingHeaders addressingHeaders;
    
    /** Should http errors be handled with reply message handler or simply throw exception */
    private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION;
    
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
        
        if (!(message.getPayload() instanceof String)) {
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
        getWebServiceTemplate().setFaultMessageResolver(new InternalFaultMessageResolver(message, endpointUri));
        
        log.info("Sending SOAP message to endpoint: '" + endpointUri + "'");
        
        boolean result; 
        // send and receive message
        if (endpointResolver != null) {
            result = getWebServiceTemplate().sendAndReceive(endpointUri, requestCallback, responseCallback);
        } else { // use default endpoint uri
            result = getWebServiceTemplate().sendAndReceive(requestCallback, responseCallback);
        }

        if (result) {
            log.info("Received SOAP response from endpoint: '" + endpointUri + "'");
            informReplyMessageHandler(responseCallback.getResponse(), message);
        } else { // must have fault in response message, error handler took care of that
            
        }
    }
    
    /**
     * Informs reply message handler for further processing 
     * of reply message.
     * @param responseMessage the reply message.
     * @param requestMessage the initial request message.
     */
    protected void informReplyMessageHandler(Message<?> responseMessage, Message<?> requestMessage) {
        if (replyMessageHandler != null) {
            log.info("Informing reply message handler for further processing");
            
            if (correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(requestMessage));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }
    
    /**
     * Handles error response messages constructing a proper response message
     * which will be propagated to the respective reply message handler for 
     * further processing.
     */
    private class InternalFaultMessageResolver implements FaultMessageResolver {

        /** Request message associated with this response error handler */
        private Message<?> requestMessage;
        
        /** The endpoint that was initially invoked */
        private String endpointUri;
        
        /**
         * Default constructor provided with request message 
         * associated with this fault resolver and endpoint uri.
         */
        public InternalFaultMessageResolver(Message<?> requestMessage, String endpointUri) {
            this.requestMessage = requestMessage;
        }
        
        /**
         * Handle fault response message according to error strategy.
         */
        public void resolveFault(WebServiceMessage webServiceResponse) throws IOException {
            if (errorHandlingStrategy.equals(ErrorHandlingStrategy.PROPAGATE)) {
                SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
                try {
                    callback.doWithMessage(webServiceResponse);
                    
                    Message<?> responseMessage = callback.getResponse();
                    
                    if (webServiceResponse instanceof SoapMessage) {
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        
                        StringResult faultPayload = new StringResult();
                        transformer.transform(((SoapMessage)webServiceResponse).getSoapBody().getFault().getSource(), faultPayload);
                        
                        responseMessage = MessageBuilder.withPayload(faultPayload.toString()).copyHeaders(responseMessage.getHeaders()).build();
                    }
                    
                    log.info("Received SOAP fault response from endpoint: '" + endpointUri + "'");
                    informReplyMessageHandler(responseMessage, requestMessage);
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException("Failed to handle fault response message", e);
                }
            } else if (errorHandlingStrategy.equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
                if (webServiceResponse instanceof SoapMessage) {
                    new SoapFaultMessageResolver().resolveFault(webServiceResponse);
                } else {
                    new SimpleFaultMessageResolver().resolveFault(webServiceResponse);
                }
            } else {
                throw new CitrusRuntimeException("Unsupported error strategy: " + errorHandlingStrategy);
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

    /**
     * Gets the errorHandlingStrategy.
     * @return the errorHandlingStrategy
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return errorHandlingStrategy;
    }

    /**
     * Sets the errorHandlingStrategy.
     * @param errorHandlingStrategy the errorHandlingStrategy to set
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
    }
}
