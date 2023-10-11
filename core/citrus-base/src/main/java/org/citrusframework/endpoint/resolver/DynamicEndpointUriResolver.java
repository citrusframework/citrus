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

package org.citrusframework.endpoint.resolver;

import java.util.Map;
import java.util.StringTokenizer;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;

/**
 * Endpoint uri resolver working on message headers. Resolver is searching for a specific header entry which holds the actual
 * target endpoint uri.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class DynamicEndpointUriResolver implements EndpointUriResolver {

    /** Default fallback uri */
    private String defaultEndpointUri;

    /**
     * Get the endpoint uri according to message header entry with fallback default uri.
     */
    public String resolveEndpointUri(Message message, String defaultUri) {
        Map<String, Object> headers = message.getHeaders();

        String requestUri;
        if (headers.containsKey(ENDPOINT_URI_HEADER_NAME)) {
            requestUri = headers.get(ENDPOINT_URI_HEADER_NAME).toString();
        } else if (StringUtils.hasText(defaultUri)) {
            requestUri = defaultUri;
        } else {
            requestUri = defaultEndpointUri;
        }

        if (requestUri == null) {
            throw new CitrusRuntimeException("Unable to resolve dynamic endpoint uri! Neither header entry '" +
                    ENDPOINT_URI_HEADER_NAME + "' nor default endpoint uri is set");
        }

        requestUri = appendRequestPath(requestUri, headers);
        requestUri = appendQueryParams(requestUri, headers);

        return requestUri;
    }

    /**
     * Appends optional request path to endpoint uri.
     * @param uri
     * @param headers
     * @return
     */
    private String appendRequestPath(String uri, Map<String, Object> headers) {
        if (!headers.containsKey(REQUEST_PATH_HEADER_NAME)) {
            return uri;
        }

        String requestUri = uri;
        String path = headers.get(REQUEST_PATH_HEADER_NAME).toString();

        while (requestUri.endsWith("/")) {
            requestUri = requestUri.substring(0, requestUri.length() - 1);
        }

        while (path.startsWith("/") && path.length() > 0) {
            path = path.length() == 1 ? "" : path.substring(1);
        }

        return requestUri + "/" + path;
    }

    /**
     * Appends one to many query param key value paris to request uri. Syntax must be: param1=value1,param2=value2.
     * Results in parameterized request uri such as http://localhost:8080/test?param1=value1&param2=value2
     * @param uri
     * @param headers
     * @return
     */
    private String appendQueryParams(String uri, Map<String, Object> headers) {
        if (!headers.containsKey(QUERY_PARAM_HEADER_NAME)) {
            return uri;
        }

        String requestUri = uri;
        StringBuilder queryParamBuilder = new StringBuilder();
        String queryParams = headers.get(QUERY_PARAM_HEADER_NAME).toString();

        StringTokenizer tok = new StringTokenizer(queryParams, ",");
        if (tok.hasMoreTokens()) {
            while (requestUri.endsWith("/")) {
                requestUri = requestUri.substring(0, requestUri.length() - 1);
            }

            queryParamBuilder.append("?").append(tok.nextToken());
        }

        while (tok.hasMoreTokens()) {
            queryParamBuilder.append("&").append(tok.nextToken());
        }

        return requestUri + queryParamBuilder.toString();
    }

    /**
     * Sets the default fallback endpoint uri.
     * @param defaultEndpointUri the defaultUri to set
     */
    public void setDefaultEndpointUri(String defaultEndpointUri) {
        this.defaultEndpointUri = defaultEndpointUri;
    }
}
