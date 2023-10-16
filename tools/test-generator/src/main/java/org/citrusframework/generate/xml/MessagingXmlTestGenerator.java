/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.xml;

import org.citrusframework.generate.provider.*;
import org.citrusframework.generate.provider.http.*;
import org.citrusframework.generate.provider.soap.*;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ws.message.SoapMessage;

import java.util.List;
import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class MessagingXmlTestGenerator<T extends MessagingXmlTestGenerator> extends XmlTestGenerator<T> {

    /** Endpoint name to use */
    private String endpoint;

    /** Sample request */
    private Message request;

    /** Sample response */
    private Message response;

    @Override
    protected List<Object> getActions() {
        List<Object> actions = super.getActions();

        if (getMode().equals(GeneratorMode.CLIENT)) {
            actions.add(getSendRequestActionProvider(request).getAction(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateOutboundMessage(request)));

            if (response != null) {
                actions.add(getReceiveResponseActionProvider(response).getAction(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateInboundMessage(response)));
            }
        } else if (getMode().equals(GeneratorMode.SERVER)) {
            actions.add(getReceiveRequestActionProvider(request).getAction(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateInboundMessage(request)));

            if (response != null) {
                actions.add(getSendResponseActionProvider(response).getAction(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateOutboundMessage(response)));
            }
        }

        return actions;
    }

    /**
     * Inbound message generation hook for subclasses.
     * @param message
     * @return
     */
    protected Message generateInboundMessage(Message message) {
        return message;
    }

    /**
     * Outbound message generation hook for subclasses.
     * @param message
     * @return
     */
    protected Message generateOutboundMessage(Message message) {
        return message;
    }

    protected <T, M extends Message> MessageActionProvider<T, M> getSendRequestActionProvider(M message) {
        if (message instanceof HttpMessage) {
            return (MessageActionProvider<T, M>) new SendHttpRequestActionProvider();
        } else if (message instanceof SoapMessage) {
            return (MessageActionProvider<T, M>) new SendSoapRequestActionProvider();
        } else {
            return (MessageActionProvider<T, M>) new SendActionProvider();
        }
    }

    protected <T, M extends Message> MessageActionProvider<T, M> getReceiveResponseActionProvider(M message) {
        if (message instanceof HttpMessage) {
            return (MessageActionProvider<T, M>) new ReceiveHttpResponseActionProvider();
        } else if (message instanceof SoapMessage) {
            return (MessageActionProvider<T, M>) new ReceiveSoapResponseActionProvider();
        } else {
            return (MessageActionProvider<T, M>) new ReceiveActionProvider();
        }
    }

    protected <T, M extends Message> MessageActionProvider<T, M> getSendResponseActionProvider(M message) {
        if (message instanceof HttpMessage) {
            return (MessageActionProvider<T, M>) new SendHttpResponseActionProvider();
        } else if (message instanceof SoapMessage) {
            return (MessageActionProvider<T, M>) new SendSoapResponseActionProvider();
        } else {
            return (MessageActionProvider<T, M>) new SendActionProvider();
        }
    }

    protected <T, M extends Message> MessageActionProvider<T, M> getReceiveRequestActionProvider(M message) {
        if (message instanceof HttpMessage) {
            return (MessageActionProvider<T, M>) new ReceiveHttpRequestActionProvider();
        } else if (message instanceof SoapMessage) {
            return (MessageActionProvider<T, M>) new ReceiveSoapRequestActionProvider();
        } else {
            return (MessageActionProvider<T, M>) new ReceiveActionProvider();
        }
    }

    /**
     * Set the endpoint to use.
     * @param endpoint
     * @return
     */
    public T withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return self;
    }

    /**
     * Set the request to use.
     * @param request
     * @return
     */
    public T withRequest(Message request) {
        this.request = request;
        return self;
    }

    /**
     * Set the response to use.
     * @param response
     * @return
     */
    public T withResponse(Message response) {
        this.response = response;
        return self;
    }

    /**
     * Adds a request header to use.
     * @param name
     * @param value
     * @return
     */
    public T addRequestHeader(String name , Object value) {
        this.request.setHeader(name, value);
        return self;
    }

    /**
     * Adds a response header to use.
     * @param name
     * @param value
     * @return
     */
    public T addResponseHeader(String name, Object value) {
        this.request.setHeader(name, value);
        return self;
    }

    /**
     * Sets the endpoint.
     *
     * @param endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the endpoint.
     *
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

}
