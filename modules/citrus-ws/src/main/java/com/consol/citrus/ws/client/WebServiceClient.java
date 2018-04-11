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
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.*;
import com.consol.citrus.ws.interceptor.LoggingClientInterceptor;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.callback.SoapRequestMessageCallback;
import com.consol.citrus.ws.message.callback.SoapResponseMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class WebServiceClient extends AbstractEndpoint implements Producer, ReplyConsumer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WebServiceClient.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public WebServiceClient() {
        this(new WebServiceEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public WebServiceClient(WebServiceEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public WebServiceEndpointConfiguration getEndpointConfiguration() {
        return (WebServiceEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        if (CollectionUtils.isEmpty(getEndpointConfiguration().getInterceptors()) && getEndpointConfiguration().getInterceptor() == null) {
            LoggingClientInterceptor loggingClientInterceptor = new LoggingClientInterceptor();
            loggingClientInterceptor.setMessageListener(context.getMessageListeners());

            getEndpointConfiguration().setInterceptor(loggingClientInterceptor);
        }

        SoapMessage soapMessage;
        if (message instanceof SoapMessage) {
            soapMessage = (SoapMessage) message;
        } else {
            soapMessage = new SoapMessage(message);
        }

        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(soapMessage);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String endpointUri;
        if (getEndpointConfiguration().getEndpointResolver() != null) {
            endpointUri = getEndpointConfiguration().getEndpointResolver().resolveEndpointUri(soapMessage, getEndpointConfiguration().getDefaultUri());
        } else { // use default uri
            endpointUri = getEndpointConfiguration().getDefaultUri();
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending SOAP message to endpoint: '" + endpointUri + "'");
            log.debug("Message to send is:\n" + soapMessage.toString());
        }

        if (!(soapMessage.getPayload() instanceof String)) {
            throw new CitrusRuntimeException("Unsupported payload type '" + soapMessage.getPayload().getClass() +
                    "' Currently only 'java.lang.String' is supported as payload type.");
        }

        SoapRequestMessageCallback requestCallback = new SoapRequestMessageCallback(soapMessage, getEndpointConfiguration(), context);

        SoapResponseMessageCallback responseCallback = new SoapResponseMessageCallback(getEndpointConfiguration(), context);
        getEndpointConfiguration().getWebServiceTemplate().setFaultMessageResolver(new InternalFaultMessageResolver(correlationKey, endpointUri, context));

        boolean result;
        // send and receive message
        if (getEndpointConfiguration().getEndpointResolver() != null) {
            result = getEndpointConfiguration().getWebServiceTemplate().sendAndReceive(endpointUri, requestCallback, responseCallback);
        } else { // use default endpoint uri
            result = getEndpointConfiguration().getWebServiceTemplate().sendAndReceive(requestCallback, responseCallback);
        }

        log.info("SOAP message was sent to endpoint: '" + endpointUri + "'");

        if (result) {
            log.info("Received SOAP response on endpoint: '" + endpointUri + "'");
            correlationManager.store(correlationKey, responseCallback.getResponse());
        } else {
            log.info("Received no SOAP response from endpoint: '" + endpointUri + "'");
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message from soap web server");
        }

        return message;
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
     * which will be propagated to the respective endpoint consumer for
     * further processing.
     */
    private class InternalFaultMessageResolver implements FaultMessageResolver {

        /** Request message associated with this response error handler */
        private String correlationKey;

        /** The endpoint that was initially invoked */
        private String endpointUri;

        /** Test context */
        private TestContext context;

        /**
         * Default constructor provided with request message
         * associated with this fault resolver and endpoint uri.
         */
        public InternalFaultMessageResolver(String correlationKey, String endpointUri, TestContext context) {
            this.correlationKey = correlationKey;
            this.endpointUri = endpointUri;
            this.context = context;
        }

        /**
         * Handle fault response message according to error strategy.
         */
        public void resolveFault(WebServiceMessage webServiceResponse) throws IOException {
            if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.PROPAGATE)) {
                SoapResponseMessageCallback callback = new SoapResponseMessageCallback(getEndpointConfiguration(), context);
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

                    log.info("Received SOAP fault response on endpoint: '" + endpointUri + "'");
                    correlationManager.store(correlationKey, responseMessage);
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

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
