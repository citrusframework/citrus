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

package com.consol.citrus.http.util;

import java.util.Map.Entry;

import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.util.MessageUtils;

/**
 * Utility methods for Http communication.
 * 
 * @author Christoph Deppisch
 */
public class HttpUtils {

    /**
     * Generates a new Http request from message.
     * @param request
     * @return
     */
    public static String generateRequest(Message<?> request) {
        StringBuffer sBuf = new StringBuffer();

        // output status line
        sBuf.append(request.getHeaders().get("HTTPMethod"));
        sBuf.append(" ").append(request.getHeaders().get("HTTPUri"));
        sBuf.append(" ").append(request.getHeaders().get("HTTPVersion")).append(HttpConstants.LINE_BREAK);

        if (!request.getHeaders().containsKey("host")) {
            sBuf.append("host: ").append(
                    request.getHeaders().get("HTTPHost") + ":"
                            + request.getHeaders().get("HTTPPort"))
                    .append(HttpConstants.LINE_BREAK);
        }

        if (!request.getHeaders().containsKey("connection")) {
            sBuf.append("connection: close").append(HttpConstants.LINE_BREAK);
        }

        if (request.getPayload() != null && request.getPayload().toString().length() > 0 && !request.getHeaders().containsKey("content-length")) {
            sBuf.append("content-length: ").append(
                    Integer.toString(request.getPayload().toString().length()))
                    .append(HttpConstants.LINE_BREAK);
        }

        // output headers
        for (Entry<String, Object> headerEntry : request.getHeaders().entrySet()) {
            if (!headerEntry.getKey().startsWith("HTTP") && !MessageUtils.isSpringInternalHeader(headerEntry.getKey())) {
                sBuf.append(headerEntry.getKey()).append(": ").append(headerEntry.getValue()).append(HttpConstants.LINE_BREAK);
            }
        }

        // output post data
        if (request.getPayload() != null && request.getPayload().toString().length() > 0) {
            sBuf.append(HttpConstants.LINE_BREAK);
            sBuf.append(request.getPayload());
        }

        // signal end
        sBuf.append(HttpConstants.LINE_BREAK);

        return sBuf.toString();
    }

    /**
     * Generates a new Http response from message.
     * @param response
     * @return
     */
    public static String generateResponse(Message<?> response) {
        String httpVersion = HttpConstants.HTTP_VERSION;
        String httpStatusCode = HttpConstants.HTTP_CODE_200;
        String httpReasonPhrase = HttpConstants.HTTP_STATUS_OK;
        
        if (response.getHeaders().get("HTTPVersion") != null && response.getHeaders().get("HTTPVersion").toString().length() > 0) {
            httpVersion = response.getHeaders().get("HTTPVersion").toString();
        }
        if (response.getHeaders().get("HTTPStatusCode") != null && response.getHeaders().get("HTTPStatusCode").toString().length() > 0) {
            httpStatusCode = response.getHeaders().get("HTTPStatusCode").toString();
        }
        if (response.getHeaders().get("HTTPReasonPhrase") != null && response.getHeaders().get("HTTPReasonPhrase").toString().length() > 0) {
            httpReasonPhrase = response.getHeaders().get("HTTPReasonPhrase").toString();
        }

        StringBuffer sBuf = new StringBuffer();

        // output status line
        sBuf.append(httpVersion);
        sBuf.append(" ").append(httpStatusCode);
        sBuf.append(" ").append(httpReasonPhrase).append(HttpConstants.LINE_BREAK);

        // output headers
        if (response.getHeaders() != null) {
            for (Entry<String, Object> headerEntry : response.getHeaders().entrySet()) {
                if (!headerEntry.getKey().startsWith("HTTP") && !MessageUtils.isSpringInternalHeader(headerEntry.getKey())) {
                    sBuf.append(headerEntry.getKey()).append(": ").append(headerEntry.getValue()).append(HttpConstants.LINE_BREAK);
                }
            }
        }

        // output content
        String content = response.getPayload().toString();
        if (StringUtils.hasText(content)) {
            sBuf.append(HttpConstants.LINE_BREAK);
            sBuf.append(content);
        }

        // signal end
        sBuf.append(HttpConstants.LINE_BREAK);

        return sBuf.toString();
    }
}
