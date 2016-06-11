/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.HttpMessageHeaders;
import org.springframework.http.HttpMethod;

/**
 * Special method for HTTP senders. This builder is used to set the Citrus special headers with special
 * meanings in a type safe manner.
 *
 * @author Christoph Deppisch
 * @since 2.3
 * @deprecated since 2.6 in favour of using {@link HttpActionBuilder}
 */
public class SendHttpMessageBuilder extends SendMessageBuilder<SendMessageAction, SendHttpMessageBuilder> {

    /**
     * Constructor delegating to the parent constructor
     *
     * @param action action defined by this definiton
     */
    public SendHttpMessageBuilder(SendMessageAction action) {
        super(action);
    }

    /**
     * Constructor using delegate test action.
     * @param action
     */
    public SendHttpMessageBuilder(DelegatingTestAction<TestAction> action) {
        super(action);
    }

    /**
     * Set the method of the request (GET, POST, ...)
     *
     * @param method method to set
     * @return self
     */
    public SendHttpMessageBuilder method(HttpMethod method) {
        header(HttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    public SendHttpMessageBuilder uri(String uri) {
        // Set the endpoint URL properly.
        header(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, uri);
        return this;
    }

    /**
     * Add a path to the endpoint URL. The path should start with a '/', any
     * multiple slashes on the concatenation point between endpoint URL and path are squeezed
     * to a single '/'. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver so that the default endpoint URI resolver, which
     * evaluates the message header <code>citrus_endpoint_uri</code> and <code>citrus_request_path</code>
     * for resolving the endpoint uri.
     *
     * @param path to set
     * @return self
     */
    public SendHttpMessageBuilder path(String path) {
        header(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, path);
        return this;
    }

    /**
     * Adds a query param to the request uri.
     * @param name
     * @param value
     * @return
     */
    public SendHttpMessageBuilder queryParam(String name, String value) {
        String queryParams;
        if (getMessageContentBuilder().getMessageHeaders().containsKey(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME)) {
            queryParams = getMessageContentBuilder().getMessageHeaders().get(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME).toString();
            queryParams += "," + name + "=" + value;
        } else {
            queryParams = name + "=" + value;
        }

        header(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME, queryParams);

        return this;
    }

    @Override
    @Deprecated
    public SendHttpMessageBuilder http() {
        return this;
    }

    @Override
    @Deprecated
    public SendSoapMessageBuilder soap() {
        throw new CitrusRuntimeException("Invalid use of http and soap action builder");
    }
}
