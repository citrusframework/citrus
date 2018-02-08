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

package com.consol.citrus.generate;

import com.consol.citrus.generate.provider.*;
import com.consol.citrus.generate.provider.http.*;
import com.consol.citrus.generate.provider.soap.*;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.ws.message.SoapMessage;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class MessagingXmlTestGenerator extends XmlTestGenerator {

    /** Actor descripbing which part (client/server) to use */
    private String actor = "client";

    /** Endpoint name to use */
    private String endpoint = "default";

    /** Sample request */
    private Message request;

    /** Sample response */
    private Message response;

    @Override
    protected List<Object> getActions() {
        List<Object> actions = super.getActions();

        if (actor.equalsIgnoreCase("client")) {
            actions.add(getSendRequestActionProvider(request).getAction(endpoint, request));

            if (response != null) {
                actions.add(getReceiveResponseActionProvider(response).getAction(endpoint, response));
            }
        } else if (actor.equalsIgnoreCase("server")) {
            actions.add(getReceiveRequestActionProvider(request).getAction(endpoint, request));

            if (response != null) {
                actions.add(getSendResponseActionProvider(response).getAction(endpoint, response));
            }
        }

        return actions;
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
     * Set the actor describing which part (client/server) to use.
     * @param actor
     * @return
     */
    public MessagingXmlTestGenerator withActor(String actor) {
        this.actor = actor;
        return this;
    }

    /**
     * Set the endpoint to use.
     * @param endpoint
     * @return
     */
    public MessagingXmlTestGenerator withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the request to use.
     * @param request
     * @return
     */
    public MessagingXmlTestGenerator withRequest(Message request) {
        this.request = request;
        return this;
    }

    /**
     * Set the response to use.
     * @param response
     * @return
     */
    public MessagingXmlTestGenerator withResponse(Message response) {
        this.response = response;
        return this;
    }

    /**
     * Adds a request header to use.
     * @param name
     * @param value
     * @return
     */
    public MessagingXmlTestGenerator addRequestHeader(String name , Object value) {
        this.request.setHeader(name, value);
        return this;
    }

    /**
     * Adds a response header to use.
     * @param name
     * @param value
     * @return
     */
    public MessagingXmlTestGenerator addResponseHeader(String name, Object value) {
        this.request.setHeader(name, value);
        return this;
    }

    /**
     * Gets the actor.
     *
     * @return
     */
    public String getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     *
     * @param actor
     */
    public void setActor(String actor) {
        this.actor = actor;
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
