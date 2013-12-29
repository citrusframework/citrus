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

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.client.WebServiceClient;
import org.springframework.integration.Message;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.mime.Attachment;

/**
 * Message sender connection as client to a WebService endpoint. The sender supports
 * SOAP attachments in contrary to the normal message senders.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class WebServiceMessageSender extends AbstractSyncMessageSender {

    /** New WebServiceClient */
    private WebServiceClient webServiceClient;

    public WebServiceMessageSender() {
        this(new WebServiceClient());
    }

    public WebServiceMessageSender(WebServiceClient webServiceClient) {
        super(webServiceClient);
        this.webServiceClient = webServiceClient;
    }

    @Override
    public void send(Message<?> message) {
        webServiceClient.createProducer().send(message);
    }

    public void send(final Message<?> message, final Attachment attachment) {
        webServiceClient.send(message, attachment);
    }

    @Override
    public Consumer createConsumer() {
        return webServiceClient.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return webServiceClient.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return webServiceClient.getEndpointConfiguration();
    }

    @Override
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        super.setReplyMessageHandler(replyMessageHandler);

        if (replyMessageHandler instanceof SoapReplyMessageReceiver) {
            ((SoapReplyMessageReceiver) replyMessageHandler).setEndpoint(webServiceClient);
        }
    }

    /**
     * Set reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        webServiceClient.getEndpointConfiguration().setCorrelator(correlator);
    }
    
    /**
     * Sets the endpoint uri resolver.
     * @param endpointResolver the endpointUriResolver to set
     */
    public void setEndpointResolver(EndpointUriResolver endpointResolver) {
        webServiceClient.getEndpointConfiguration().setEndpointResolver(endpointResolver);
    }

    /**
     * Sets the ws addressing headers for this message sender.
     * @param addressingHeaders the addressingHeaders to set
     */
    public void setAddressingHeaders(WsAddressingHeaders addressingHeaders) {
        webServiceClient.getEndpointConfiguration().setAddressingHeaders(addressingHeaders);
    }

    /**
     * Gets the errorHandlingStrategy.
     * @return the errorHandlingStrategy
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return webServiceClient.getEndpointConfiguration().getErrorHandlingStrategy();
    }

    /**
     * Sets the errorHandlingStrategy.
     * @param errorHandlingStrategy the errorHandlingStrategy to set
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        webServiceClient.getEndpointConfiguration().setErrorHandlingStrategy(errorHandlingStrategy);
    }

    /**
     * Gets the correlator.
     * @return the correlator the correlator to get.
     */
    public ReplyMessageCorrelator getCorrelator() {
        return webServiceClient.getEndpointConfiguration().getCorrelator();
    }

    /**
     * Gets the endpointResolver.
     * @return the endpointResolver the endpointResolver to get.
     */
    public EndpointUriResolver getEndpointResolver() {
        return webServiceClient.getEndpointConfiguration().getEndpointResolver();
    }

    /**
     * Gets the addressingHeaders.
     * @return the addressingHeaders the addressingHeaders to get.
     */
    public WsAddressingHeaders getAddressingHeaders() {
        return webServiceClient.getEndpointConfiguration().getAddressingHeaders();
    }

    /**
     * Sets the web service template.
     * @param webServiceTemplate
     */
    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        webServiceClient.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
    }

    /**
     * Sets the message factory.
     * @param messageFactory
     */
    public void setMessageFactory(WebServiceMessageFactory messageFactory) {
        webServiceClient.getEndpointConfiguration().setMessageFactory(messageFactory);
    }

    /**
     * Sets the message sender.
     * @param messageSender
     */
    public void setMessageSender(org.springframework.ws.transport.WebServiceMessageSender messageSender) {
        webServiceClient.getEndpointConfiguration().setMessageSender(messageSender);
    }

    /**
     * Gets the default uri.
     * @param defaultUri
     */
    public void setDefaultUri(String defaultUri) {
        webServiceClient.getEndpointConfiguration().setDefaultUri(defaultUri);
    }

    /**
     * Gets the client interceptors.
     * @return
     */
    public ClientInterceptor[] getInterceptors() {
        return webServiceClient.getEndpointConfiguration().getInterceptors();
    }

    /**
     * Sets the client interceptors.
     * @param interceptors
     */
    public void setInterceptors(ClientInterceptor[] interceptors) {
        webServiceClient.getEndpointConfiguration().setInterceptors(interceptors);
    }

    /**
     * Gets the web service client.
     * @return
     */
    public WebServiceClient getWebServiceClient() {
        return webServiceClient;
    }
}
