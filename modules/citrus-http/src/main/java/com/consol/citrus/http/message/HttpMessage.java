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

package com.consol.citrus.http.message;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class HttpMessage extends DefaultMessage {

    /**
     * Constructs copy of given message.
     * @param message
     */
    public HttpMessage(Message message) {
        super(message);
    }

    /**
     * Default message using message payload.
     * @param payload
     */
    public HttpMessage(Object payload) {
        super(payload);
    }

    /**
     * Default message using message payload and headers.
     * @param payload
     * @param headers
     */
    public HttpMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Sets the Http request method.
     * @param method
     */
    public HttpMessage setRequestMethod(HttpMethod method) {
        setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Gets the Http request method.
     * @return
     */
    public HttpMethod getRequestMethod() {
        Object method = getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD);

        if (method != null) {
            return HttpMethod.valueOf(method.toString());
        }

        return null;
    }

    /**
     * Sets the Http request request uri.
     * @param requestUri
     */
    public HttpMessage setRequestUri(String requestUri) {
        setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, requestUri);
        return this;
    }

    /**
     * Gets the Http request request uri.
     * @return
     */
    public String getRequestUri() {
        Object requestUri = getHeader(HttpMessageHeaders.HTTP_REQUEST_URI);

        if (requestUri != null) {
            return requestUri.toString();
        }

        return null;
    }

    /**
     * Sets the Http request context path.
     * @param contextPath
     */
    public HttpMessage setContextPath(String contextPath) {
        setHeader(HttpMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
        return this;
    }

    /**
     * Gets the Http request context path.
     * @return
     */
    public String getContextPath() {
        Object contextPath = getHeader(HttpMessageHeaders.HTTP_CONTEXT_PATH);

        if (contextPath != null) {
            return contextPath.toString();
        }

        return null;
    }

    /**
     * Sets the Http request query params.
     * @param queryParams
     */
    public HttpMessage setQueryParams(String queryParams) {
        setHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, queryParams);
        return this;
    }

    /**
     * Gets the Http request query params.
     * @return
     */
    public String getQueryParams() {
        Object queryParams = getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS);

        if (queryParams != null) {
            return queryParams.toString();
        }

        return null;
    }

    /**
     * Sets the Http response status code.
     * @param statusCode
     */
    public HttpMessage setStatusCode(HttpStatus statusCode) {
        setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, Integer.valueOf(statusCode.value()));
        return this;
    }

    /**
     * Gets the Http response status code.
     * @return
     */
    public HttpStatus getStatusCode() {
        Object statusCode = getHeader(HttpMessageHeaders.HTTP_STATUS_CODE);

        if (statusCode != null) {
            if (statusCode instanceof HttpStatus) {
                return (HttpStatus) statusCode;
            } else if (statusCode instanceof Integer) {
                return HttpStatus.valueOf((Integer) statusCode);
            } else {
                return HttpStatus.valueOf(Integer.valueOf(statusCode.toString()));
            }
        }

        return null;
    }

    /**
     * Sets the Http response reason phrase.
     * @param reasonPhrase
     */
    public HttpMessage setReasonPhrase(String reasonPhrase) {
        setHeader(HttpMessageHeaders.HTTP_REASON_PHRASE, reasonPhrase);
        return this;
    }

    /**
     * Gets the Http response reason phrase.
     * @return
     */
    public String getReasonPhrase() {
        Object reasonPhrase = getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE);

        if (reasonPhrase != null) {
            return reasonPhrase.toString();
        }

        return null;
    }

    /**
     * Sets the Http version.
     * @param version
     */
    public HttpMessage setVersion(String version) {
        setHeader(HttpMessageHeaders.HTTP_VERSION, version);
        return this;
    }

    /**
     * Gets the Http version.
     * @return
     */
    public String getVersion() {
        Object version = getHeader(HttpMessageHeaders.HTTP_VERSION);

        if (version != null) {
            return version.toString();
        }

        return null;
    }

    @Override
    public HttpMessage setHeader(String headerName, Object headerValue) {
        return (HttpMessage) super.setHeader(headerName, headerValue);
    }
}
