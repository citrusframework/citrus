/*
 * Copyright the original author or authors.
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

package org.citrusframework.cucumber.steps.http;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.MessageType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

public interface HttpSteps {

    /**
     * Maps content type value to Citrus message type used later on for selecting
     * the right message validator implementation.
     *
     * @param contentType
     * @return
     */
    default String getMessageType(String contentType) {
        List<MediaType> binaryMediaTypes = Arrays.asList(MediaType.APPLICATION_OCTET_STREAM,
                MediaType.APPLICATION_PDF,
                MediaType.IMAGE_GIF,
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.valueOf("application/zip"));

        if (contentType.equals(MediaType.APPLICATION_JSON_VALUE) ||
                contentType.equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
            return MessageType.JSON.name();
        } else if (contentType.equals(MediaType.APPLICATION_XML_VALUE)) {
            return MessageType.XML.name();
        } else if (contentType.equals(MediaType.APPLICATION_XHTML_XML_VALUE)) {
            return MessageType.XHTML.name();
        } else if (contentType.equals(MediaType.TEXT_PLAIN_VALUE) ||
                contentType.equals(MediaType.TEXT_HTML_VALUE)) {
            return MessageType.PLAINTEXT.name();
        } else if (binaryMediaTypes.stream().anyMatch(mediaType -> contentType.equals(mediaType.getType()))) {
            return MessageType.BINARY.name();
        }

        return CitrusSettings.DEFAULT_MESSAGE_TYPE;
    }

    /**
     * Prepare request message with given body, headers, method and path.
     * @param body
     * @param headers
     * @param method
     * @param path
     * @param context
     * @return
     */
    default HttpMessage createRequest(String body, Map<String, String> headers, Map<String, String> params, String method, String path, TestContext context) {
        HttpMessage request = new HttpMessage();
        request.method(HttpMethod.valueOf(method));

        if (StringUtils.hasText(path)) {
            request.path(path);
        }

        if (StringUtils.hasText(body)) {
            request.setPayload(context.replaceDynamicContentInString(body));
        }

        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            request.setHeader(headerEntry.getKey(), headerEntry.getValue());
        }

        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            request.queryParam(paramEntry.getKey(), paramEntry.getValue());
        }

        return request;
    }

    /**
     * Prepare response message with given body, headers and status.
     * @param body
     * @param headers
     * @param status
     * @param context
     * @return
     */
    default HttpMessage createResponse(String body, Map<String, String> headers, Integer status, TestContext context) {
        HttpMessage response = new HttpMessage();
        response.status(HttpStatus.valueOf(status));

        if (StringUtils.hasText(body)) {
            response.setPayload(context.replaceDynamicContentInString(body));
        }

        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            response.setHeader(headerEntry.getKey(), context.replaceDynamicContentInString(headerEntry.getValue()));
        }

        return response;
    }
}
