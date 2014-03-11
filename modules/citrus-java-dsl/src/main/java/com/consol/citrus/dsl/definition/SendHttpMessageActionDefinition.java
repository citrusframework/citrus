/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.integration.Message;
import org.springframework.oxm.Marshaller;

/**
 * Special method for HTTP senders. This definition is used to set the Citrus special headers with special
 * meanings in a type safe manner.
 *
 * @author roland
 * @since 1.4
 */
public class SendHttpMessageActionDefinition extends SendMessageActionDefinition {

    /**
     * Constructor delegating to the parent constructor
     *
     * @param action action defined by this definiton
     * @param positionHandle position within the list of test actions.
     */
    public SendHttpMessageActionDefinition(SendMessageAction action, PositionHandle positionHandle) {
        super(action, positionHandle);
    }

    /**
     * Set the method of the request (GET, POST, ...)
     *
     * @param method method to set
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition method(HttpMethod method) {
        header(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Set the endpoint URI for the request
     *
     * @param uri absolute URI to use for the endpoint
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition uri(String uri) {
        header(CitrusHttpMessageHeaders.HTTP_REQUEST_URI,uri);
        return this;
    }

    @Override
    public SendHttpMessageActionDefinition fork(boolean forkMode) {
        return (SendHttpMessageActionDefinition) super.fork(forkMode);
    }

    @Override
    public SendHttpMessageActionDefinition message(Message<String> message) {
        return (SendHttpMessageActionDefinition) super.message(message);
    }

    @Override
    public SendHttpMessageActionDefinition payload(Object payload, Marshaller marshaller) {
        return (SendHttpMessageActionDefinition) super.payload(payload, marshaller);
    }

    @Override
    public SendHttpMessageActionDefinition payload(Resource payloadResource) {
        return (SendHttpMessageActionDefinition) super.payload(payloadResource);
    }

    @Override
    public SendHttpMessageActionDefinition payload(String payload) {
        return (SendHttpMessageActionDefinition) super.payload(payload);
    }

    @Override
    public SendHttpMessageActionDefinition header(String name, Object value) {
        return (SendHttpMessageActionDefinition) super.header(name, value);
    }

    @Override
    public SendHttpMessageActionDefinition description(String description) {
        return (SendHttpMessageActionDefinition) super.description(description);
    }

    @Override
    public SendHttpMessageActionDefinition extractFromHeader(String headerName, String variable) {
        return (SendHttpMessageActionDefinition) super.extractFromHeader(headerName, variable);
    }

    @Override
    public SendHttpMessageActionDefinition extractFromPayload(String xpath, String variable) {
        return (SendHttpMessageActionDefinition) super.extractFromPayload(xpath, variable);
    }

    @Override
    public SendHttpMessageActionDefinition http() {
        return this;
    }

    @Override
    public SendSoapMessageActionDefinition soap() {
        throw new CitrusRuntimeException("Invalid use of http and soap action definition");
    }
}
