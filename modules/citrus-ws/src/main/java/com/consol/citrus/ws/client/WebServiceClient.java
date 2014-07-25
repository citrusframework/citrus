/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.ws.client;

import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.messaging.*;
import com.consol.citrus.ws.message.callback.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.*;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Client sends SOAP WebService messages to some server endpoint via Http protocol. Client waits for synchronous
 * SOAP response message.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class WebServiceClient extends AbstractEndpoint implements Producer, ReplyConsumer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WebServiceClient.class);

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor initializing endpoint configuration.
     */
    public WebServiceClient() {
        super(new WebServiceEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public WebServiceClient(WebServiceEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public WebServiceEndpointConfiguration getEndpointConfiguration() {
        return (WebServiceEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
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
        if (getEndpointConfiguration().getEndpointResolver() != null) {
            endpointUri = getEndpointConfiguration().getEndpointResolver().resolveEndpointUri(message, getEndpointConfiguration().getDefaultUri());
        } else { // use default uri
            endpointUri = getEndpointConfiguration().getDefaultUri();
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
        if (getEndpointConfiguration().getAddressingHeaders() == null) {
            requestCallback = new SoapRequestMessageCallback(message, attachment);
        } else {
            requestCallback = new WsAddressingRequestMessageCallback(message,
                    attachment, getEndpointConfiguration().getAddressingHeaders());
        }

        SoapResponseMessageCallback responseCallback = new SoapResponseMessageCallback();
        getEndpointConfiguration().getWebServiceTemplate().setFaultMessageResolver(new InternalFaultMessageResolver(message, endpointUri));

        log.info("Sending SOAP message to endpoint: '" + endpointUri + "'");

        boolean result;
        // send and receive message
        if (getEndpointConfiguration().getEndpointResolver() != null) {
            result = getEndpointConfiguration().getWebServiceTemplate().sendAndReceive(endpointUri, requestCallback, responseCallback);
        } else { // use default endpoint uri
            result = getEndpointConfiguration().getWebServiceTemplate().sendAndReceive(requestCallback, responseCallback);
        }

        if (result) {
            log.info("Received SOAP response from endpoint: '" + endpointUri + "'");
            onReplyMessage(message, responseCallback.getResponse());
        } else {
            log.info("No SOAP response from endpoint: '" + endpointUri + "'");
        }
    }

    @Override
    public Message<?> receive() {
        return receive("", getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receive(String selector) {
        return receive(selector, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receive(long timeout) {
        return receive("", timeout);
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        long timeLeft = timeout;
        Message<?> message = findReplyMessage(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= getEndpointConfiguration().getPollingInterval();

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(selector);
        }

        return message;
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void onReplyMessage(String correlationKey, Message<?> replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void onReplyMessage(Message<?> requestMessage, Message<?> replyMessage) {
        if (getEndpointConfiguration().getCorrelator() != null) {
            onReplyMessage(getEndpointConfiguration().getCorrelator().getCorrelationKey(requestMessage), replyMessage);
        } else {
            onReplyMessage("", replyMessage);
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message<?> findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     *
     * @return
     */
    @Override
    public SelectiveConsumer createConsumer() {
        return this;
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
            this.endpointUri = endpointUri;
        }

        /**
         * Handle fault response message according to error strategy.
         */
        public void resolveFault(WebServiceMessage webServiceResponse) throws IOException {
            if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.PROPAGATE)) {
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
                    onReplyMessage(requestMessage, responseMessage);
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException("Failed to handle fault response message", e);
                }
            } else if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
                if (webServiceResponse instanceof SoapMessage) {
                    new SoapFaultMessageResolver().resolveFault(webServiceResponse);
                } else {
                    new SimpleFaultMessageResolver().resolveFault(webServiceResponse);
                }
            } else {
                throw new CitrusRuntimeException("Unsupported error strategy: " + getEndpointConfiguration().getErrorHandlingStrategy());
            }
        }

    }

}
