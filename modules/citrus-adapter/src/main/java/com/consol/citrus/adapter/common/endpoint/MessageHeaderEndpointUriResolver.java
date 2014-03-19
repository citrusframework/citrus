/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.adapter.common.endpoint;

import org.springframework.integration.Message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import org.springframework.integration.MessageHeaders;

/**
 * Endpoint uri resolver working on message headers. Resolver is searching for a specific header entry which holds the actual
 * target endpoint uri.
 * 
 * @author Christoph Deppisch
 */
public class MessageHeaderEndpointUriResolver implements EndpointUriResolver {

    /** Static header entry name specifying the dynamic endpoint uri */
    public static final String ENDPOINT_URI_HEADER_NAME = CitrusMessageHeaders.PREFIX + "endpoint_uri";
    public static final String ENDPOINT_PATH_HEADER_NAME = CitrusMessageHeaders.PREFIX + "endpoint_path";

    /** Default fallback uri */
    private String defaultEndpointUri;
    
    /**
     * Get the endpoint uri according to message header entry.
     */
    public String resolveEndpointUri(Message<?> message) {
        return resolveEndpointUri(message,defaultEndpointUri);
    }
    
    /**
     * Get the endpoint uri according to message header entry with fallback default uri.
     */
    public String resolveEndpointUri(Message<?> message, String defaultUri) {
        MessageHeaders headers = message.getHeaders();
        String uri = headers.containsKey(ENDPOINT_URI_HEADER_NAME) ?
                headers.get(ENDPOINT_URI_HEADER_NAME).toString() :
                defaultUri;

        if (uri == null) {
            throw new CitrusRuntimeException("Unable to resolve dynamic endpoint uri for this message - missing header entry '" +
                    ENDPOINT_URI_HEADER_NAME + "' specifying the endpoint uri neither default endpoint uri is set");
        }

        return headers.containsKey(ENDPOINT_PATH_HEADER_NAME) ?
                appendPath(uri, headers.get(ENDPOINT_PATH_HEADER_NAME).toString()) :
                uri;
    }

    private String appendPath(String uri, String path) {
        while (uri.endsWith("/")) {
            uri = uri.substring(0,uri.length() - 1);
            }
        while (path.startsWith("/") && path.length() > 0) {
            path = path.length() == 1 ? "" : path.substring(1);
        }
        return uri + "/" + path;
    }

    /**
     * Sets the default fallback endpoint uri.
     * @param defaultEndpointUri the defaultUri to set
     */
    public void setDefaultEndpointUri(String defaultEndpointUri) {
        this.defaultEndpointUri = defaultEndpointUri;
    }
}
