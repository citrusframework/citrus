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

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.HttpMessageHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * Spacial HTTP receive message action builder offers special HTTP specific properties such as
 * method and request params.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveHttpMessageBuilder extends ReceiveMessageBuilder<ReceiveMessageAction, ReceiveHttpMessageBuilder> {

    /** HTTP query parameters */
    private String queryParams;

    /**
     * Default constructor using test action, basic application context and position handle.
     *
     * @param action
     */
    public ReceiveHttpMessageBuilder(ReceiveMessageAction action) {
        super(action);
    }

    /**
     * Validates the response status received.
     *
     * @param status received response status.
     * @return self
     */
    public ReceiveHttpMessageBuilder status(HttpStatus status) {
        header(HttpMessageHeaders.HTTP_STATUS_CODE, String.valueOf(status.value()));
        header(HttpMessageHeaders.HTTP_REASON_PHRASE, status.getReasonPhrase());
        return this;
    }

    /**
     * Validates the HTTP version.
     *
     * @param version the HTTP version string
     * @return self
     */
    public ReceiveHttpMessageBuilder version(String version) {
        header(HttpMessageHeaders.HTTP_VERSION, version);
        return this;
    }

    /**
     * Set the method of the request (GET, POST, ...)
     *
     * @param method method to set
     * @return self
     */
    public ReceiveHttpMessageBuilder method(HttpMethod method) {
        header(HttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Validates the request uri used.
     *
     * @param uri absolute URI for the endpoint
     * @return self
     */
    public ReceiveHttpMessageBuilder uri(String uri) {
        header(HttpMessageHeaders.HTTP_REQUEST_URI, uri);
        return this;
    }

    /**
     * Validates the request context path used.
     *
     * @param contextPath context path for the endpoint
     * @return self
     */
    public ReceiveHttpMessageBuilder contextPath(String contextPath) {
        header(HttpMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
        return this;
    }

    /**
     * Validates that a given query param is present in the request uri.
     * @param name
     * @param value
     * @return
     */
    public ReceiveHttpMessageBuilder queryParam(String name, String value) {
        if (!StringUtils.hasText(name)) {
            throw new CitrusRuntimeException("Invalid query param name - must not be empty!");
        }

        if (StringUtils.hasText(queryParams)) {
            queryParams += "," + name + "=" + value;
        } else {
            queryParams = name + "=" + value;
        }

        header(HttpMessageHeaders.HTTP_QUERY_PARAMS, queryParams);
        return this;
    }


    @Override
    public ReceiveSoapMessageBuilder soap() {
        throw new CitrusRuntimeException("Invalid use of http and soap action builder");
    }

    @Override
    public ReceiveHttpMessageBuilder http() {
        return this;
    }

}
