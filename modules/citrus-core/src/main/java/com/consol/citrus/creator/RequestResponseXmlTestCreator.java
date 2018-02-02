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
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class RequestResponseXmlTestCreator extends XmlTestCreator {

    /** Sample request */
    private String request;

    /** Sample response */
    private String response;

    @Override
    protected List<Object> getActions() {
        List<Object> actions = super.getActions();

        actions.add(getSendRequestAction());

        if (StringUtils.hasText(response)) {
            actions.add(getReceiveResponseAction());
        }

        return actions;
    }

    /**
     * Creates test action that receives the response message.
     * @return
     */
    protected Object getReceiveResponseAction() {
        ReceiveModel receive = new ReceiveModel();
        receive.setEndpoint("TODO:response-receiver");
        ReceiveModel.Message receiveMessage = new ReceiveModel.Message();
        receiveMessage.setData(response);
        receive.setMessage(receiveMessage);
        return receive;
    }

    /**
     * Creates test action that send the request.
     * @return
     */
    protected Object getSendRequestAction() {
        SendModel send = new SendModel();
        send.setEndpoint("TODO:request-sender");
        SendModel.Message sendMessage = new SendModel.Message();
        sendMessage.setData(request);
        send.setMessage(sendMessage);
        return send;
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
}
