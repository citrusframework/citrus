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

package com.consol.citrus.creator;

import com.consol.citrus.model.testcase.core.ReceiveModel;
import com.consol.citrus.model.testcase.core.SendModel;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RequestResponseXmlTestCreator extends XmlTestCreator {

    /** Actor descripbing which part (client/server) to use */
    private String actor = "client";

    /** Endpoint name to use */
    private String endpoint = "default";

    /** Sample request */
    private String request;
    private Map<String, Object> requestHeaders;

    /** Sample response */
    private String response;
    private Map<String, Object> responseHeaders;

    @Override
    protected List<Object> getActions() {
        List<Object> actions = super.getActions();

        if (actor.equalsIgnoreCase("client")) {
            actions.add(getSendRequestAction());

            if (StringUtils.hasText(response) || !CollectionUtils.isEmpty(responseHeaders)) {
                actions.add(getReceiveResponseAction());
            }
        } else if (actor.equalsIgnoreCase("server")) {
            actions.add(getReceiveRequestAction());

            if (StringUtils.hasText(response) || !CollectionUtils.isEmpty(responseHeaders)) {
                actions.add(getSendResponseAction());
            }
        }

        return actions;
    }

    /**
     * Creates test action that sends the response message.
     * @return
     */
    protected Object getSendResponseAction() {
        SendModel send = new SendModel();
        send.setEndpoint(endpoint);
        SendModel.Message sendMessage = new SendModel.Message();
        sendMessage.setData(response);
        send.setMessage(sendMessage);

        if (!CollectionUtils.isEmpty(responseHeaders)) {
            SendModel.Header header = new SendModel.Header();

            responseHeaders.forEach((key, value) -> {
                SendModel.Header.Element element = new SendModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());

                if (!value.getClass().equals(String.class)) {
                    element.setType(value.getClass().getName());
                }

                header.getElements().add(element);
            });

            send.setHeader(header);
        }

        return send;
    }

    /**
     * Creates test action that receives the response message.
     * @return
     */
    protected Object getReceiveResponseAction() {
        ReceiveModel receive = new ReceiveModel();
        receive.setEndpoint(endpoint);
        ReceiveModel.Message receiveMessage = new ReceiveModel.Message();
        receiveMessage.setData(response);
        receive.setMessage(receiveMessage);

        if (!CollectionUtils.isEmpty(responseHeaders)) {
            ReceiveModel.Header header = new ReceiveModel.Header();

            responseHeaders.forEach((key, value) -> {
                ReceiveModel.Header.Element element = new ReceiveModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());
                header.getElements().add(element);
            });

            receive.setHeader(header);
        }

        return receive;
    }

    /**
     * Creates test action that sends the request.
     * @return
     */
    protected Object getSendRequestAction() {
        SendModel send = new SendModel();
        send.setEndpoint(endpoint);
        SendModel.Message sendMessage = new SendModel.Message();
        sendMessage.setData(request);
        send.setMessage(sendMessage);

        if (!CollectionUtils.isEmpty(requestHeaders)) {
            SendModel.Header header = new SendModel.Header();

            requestHeaders.forEach((key, value) -> {
                SendModel.Header.Element element = new SendModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());

                if (!value.getClass().equals(String.class)) {
                    element.setType(value.getClass().getName());
                }

                header.getElements().add(element);
            });

            send.setHeader(header);
        }

        return send;
    }

    /**
     * Creates test action that receives the request.
     * @return
     */
    protected Object getReceiveRequestAction() {
        ReceiveModel receive = new ReceiveModel();
        receive.setEndpoint(endpoint);
        ReceiveModel.Message receiveMessage = new ReceiveModel.Message();
        receiveMessage.setData(request);
        receive.setMessage(receiveMessage);

        if (!CollectionUtils.isEmpty(requestHeaders)) {
            ReceiveModel.Header header = new ReceiveModel.Header();

            requestHeaders.forEach((key, value) -> {
                ReceiveModel.Header.Element element = new ReceiveModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());
                header.getElements().add(element);
            });

            receive.setHeader(header);
        }

        return receive;
    }

    /**
     * Set the actor describing which part (client/server) to use.
     * @param actor
     * @return
     */
    public RequestResponseXmlTestCreator withActor(String actor) {
        this.actor = actor;
        return this;
    }

    /**
     * Set the endpoint to use.
     * @param endpoint
     * @return
     */
    public RequestResponseXmlTestCreator withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the request to use.
     * @param request
     * @return
     */
    public RequestResponseXmlTestCreator withRequest(String request) {
        this.request = request;
        return this;
    }

    /**
     * Set the response to use.
     * @param response
     * @return
     */
    public RequestResponseXmlTestCreator withResponse(String response) {
        this.response = response;
        return this;
    }

    /**
     * Set the request headers to use.
     * @param headers
     * @return
     */
    public RequestResponseXmlTestCreator withRequestHeaders(Map<String, Object> headers) {
        this.requestHeaders = headers;
        return this;
    }

    /**
     * Set the response headers to use.
     * @param headers
     * @return
     */
    public RequestResponseXmlTestCreator withResponseHeaders(Map<String, Object> headers) {
        this.responseHeaders = headers;
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

    /**
     * Sets the request.
     *
     * @param request
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * Gets the request.
     *
     * @return
     */
    public String getRequest() {
        return request;
    }

    /**
     * Sets the response.
     *
     * @param response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Gets the response.
     *
     * @return
     */
    public String getResponse() {
        return response;
    }

    /**
     * Gets the requestHeaders.
     *
     * @return
     */
    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Sets the requestHeaders.
     *
     * @param requestHeaders
     */
    public void setRequestHeaders(Map<String, Object> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * Gets the responseHeaders.
     *
     * @return
     */
    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets the responseHeaders.
     *
     * @param responseHeaders
     */
    public void setResponseHeaders(Map<String, Object> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}
