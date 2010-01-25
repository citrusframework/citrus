/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
            if (!headerEntry.getKey().startsWith("HTTP") && !MessageUtils.isSpringIntegrationHeaderEntry(headerEntry.getKey())) {
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
                if (!headerEntry.getKey().startsWith("HTTP") && !MessageUtils.isSpringIntegrationHeaderEntry(headerEntry.getKey())) {
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
