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
import com.consol.citrus.adapter.common.endpoint.MessageHeaderEndpointUriResolver;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import org.springframework.http.HttpMethod;

/**
 * Special method for HTTP senders. This definition is used to set the Citrus special headers with special
 * meanings in a type safe manner.
 *
 * @author roland
 * @since 1.4
 */
public class SendHttpMessageActionDefinition extends SendMessageActionDefinition<SendMessageAction,SendHttpMessageActionDefinition> {

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
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition uri(String uri) {
        // Set the endpoint URL properly.
        header(MessageHeaderEndpointUriResolver.ENDPOINT_URI_HEADER_NAME,uri);
        return this;
    }

    /**
     * Add a path to the endpoint URL. The path should start with a '/', any
     * multiple slashes on the concatenation point between endpoint URL and path are squeezed
     * to a single '/'. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver so that the default endpoint URI resolver, which
     * evaluates the message header <code>citrus_endpoint_uri</code> and <code>citrus_endpoint_path</code>
     * for resolving the endpoint uri.
     *
     * @param path to set
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition path(String path) {
        header(MessageHeaderEndpointUriResolver.ENDPOINT_PATH_HEADER_NAME,path);
        return this;
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
