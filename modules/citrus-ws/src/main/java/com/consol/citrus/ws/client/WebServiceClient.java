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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.*;
import com.consol.citrus.ws.interceptor.LoggingClientInterceptor;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.callback.SoapRequestMessageCallback;
import com.consol.citrus.ws.message.callback.SoapResponseMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.client.core.SimpleFaultMessageResolver;
import org.springframework.ws.soap.client.core.SoapFaultMessageResolver;
import org.springframework.xml.transform.StringResult;

import javax.xml.transform.*;
import java.io.IOException;

/**
 * Client sends SOAP WebService messages to some server endpoint via Http protocol. Client waits for synchronous
 * SOAP response message.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class WebServiceClient extends AbstractEndpoint implements Producer, ReplyConsumer, InitializingBean {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WebServiceClient.class);

    /** Store of reply messages */
    private CorrelationManager<Message> replyManager = new DefaultCorrelationManager<Message>();

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
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        SoapMessage soapMessage;
        if (message instanceof SoapMessage) {
            soapMessage = (SoapMessage) message;
        } else {
            soapMessage = new SoapMessage(message);
        }

        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(soapMessage);
        context.saveCorrelationKey(correlationKey, this);

        String endpointUri;
        if (getEndpointConfiguration().getEndpointResolver() != null) {
            endpointUri = getEndpointConfiguration().getEndpointResolver().resolveEndpointUri(soapMessage, getEndpointConfiguration().getDefaultUri());
        } else { // use default uri
            endpointUri = getEndpointConfiguration().getDefaultUri();
        }

        log.info("Sending SOAP message to endpoint: '" + endpointUri + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + soapMessage.toString());
        }

        if (!(soapMessage.getPayload() instanceof String)) {
            throw new CitrusRuntimeException("Unsupported payload type '" + soapMessage.getPayload().getClass() +
                    "' Currently only 'java.lang.String' is supported as payload type.");
        }

        SoapRequestMessageCallback requestCallback = new SoapRequestMessageCallback(soapMessage, getEndpointConfiguration());

        SoapResponseMessageCallback responseCallback = new SoapResponseMessageCallback(getEndpointConfiguration());
        getEndpointConfiguration().getWebServiceTemplate().setFaultMessageResolver(new InternalFaultMessageResolver(correlationKey, endpointUri));

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
            onReplyMessage(correlationKey, responseCallback.getResponse());
        } else {
            log.info("No SOAP response from endpoint: '" + endpointUri + "'");
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context.getCorrelationKey(this), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(context.getCorrelationKey(this), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        long timeLeft = timeout;
        Message message = findReplyMessage(selector);

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

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving WebService response from from server");
        }

        return message;
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void onReplyMessage(String correlationKey, Message replyMessage) {
        replyManager.store(correlationKey, replyMessage);
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message findReplyMessage(String correlationKey) {
        return replyManager.find(correlationKey);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(getEndpointConfiguration().getInterceptors()) && getEndpointConfiguration().getInterceptor() == null) {
            LoggingClientInterceptor loggingClientInterceptor = new LoggingClientInterceptor();
            loggingClientInterceptor.setMessageListener(getMessageListener());

            getEndpointConfiguration().setInterceptor(loggingClientInterceptor);
        }
    }

    /**
     * Handles error response messages constructing a proper response message
     * which will be propagated to the respective reply message handler for
     * further processing.
     */
    private class InternalFaultMessageResolver implements FaultMessageResolver {

        /** Request message associated with this response error handler */
        private String correlationKey;

        /** The endpoint that was initially invoked */
        private String endpointUri;

        /**
         * Default constructor provided with request message
         * associated with this fault resolver and endpoint uri.
         */
        public InternalFaultMessageResolver(String correlationKey, String endpointUri) {
            this.correlationKey = correlationKey;
            this.endpointUri = endpointUri;
        }

        /**
         * Handle fault response message according to error strategy.
         */
        public void resolveFault(WebServiceMessage webServiceResponse) throws IOException {
            if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.PROPAGATE)) {
                SoapResponseMessageCallback callback = new SoapResponseMessageCallback(getEndpointConfiguration());
                try {
                    callback.doWithMessage(webServiceResponse);

                    Message responseMessage = callback.getResponse();

                    if (webServiceResponse instanceof org.springframework.ws.soap.SoapMessage) {
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();

                        StringResult faultPayload = new StringResult();
                        transformer.transform(((org.springframework.ws.soap.SoapMessage)webServiceResponse).getSoapBody().getFault().getSource(), faultPayload);

                        responseMessage.setPayload(faultPayload.toString());
                    }

                    log.info("Received SOAP fault response from endpoint: '" + endpointUri + "'");
                    onReplyMessage(correlationKey, responseMessage);
                } catch (TransformerException e) {
                    throw new CitrusRuntimeException("Failed to handle fault response message", e);
                }
            } else if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
                if (webServiceResponse instanceof org.springframework.ws.soap.SoapMessage) {
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
