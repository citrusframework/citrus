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


import com.consol.citrus.message.ReplyMessageReceiver;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import org.springframework.integration.Message;

/**
 * Reply message receiver implementation for receiving SOAP WebService messages. We introduced this special subclass
 * in order to separate message receiver definition for SOAP messages from other message protocols. This is 
 * because we have special SOAP validation capabilities that can be used e.g. in Java DSL.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class SoapReplyMessageReceiver extends ReplyMessageReceiver {

    /**
     * Default constructor.
     */
    public SoapReplyMessageReceiver() {
        this(new WebServiceClient());
    }

    /**
     * Default constructor with Jms endpoint.
     * @param endpoint
     */

    public SoapReplyMessageReceiver(WebServiceClient endpoint) {
        super(endpoint);
    }

    @Override
    public WebServiceClient getEndpoint() {
        return (WebServiceClient) super.getEndpoint();
    }

    @Override
    public WebServiceEndpointConfiguration getEndpointConfiguration() {
        return (WebServiceEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return getEndpoint().createConsumer().receive("", getEndpointConfiguration().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return getEndpoint().createConsumer().receive("", timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return getEndpoint().createConsumer().receive(selector, getEndpointConfiguration().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return getEndpoint().createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        getEndpoint().onReplyMessage(correlationKey, replyMessage);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message)
     */
    public void onReplyMessage(Message<?> replyMessage) {
        getEndpoint().onReplyMessage("", replyMessage);
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return getEndpointConfiguration().getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        getEndpointConfiguration().setPollingInterval(pollingInterval);
    }

}
