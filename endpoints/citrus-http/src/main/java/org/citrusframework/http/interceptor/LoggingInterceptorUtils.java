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

package org.citrusframework.http.interceptor;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;

import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.message.MessageType;

import static java.nio.charset.StandardCharsets.UTF_8;

class LoggingInterceptorUtils {

    private LoggingInterceptorUtils() {
        // prevent instantiation of utility class
    }

    public static String getBodyContent(byte[] body, String contentType, String contentEncoding) {
        if (isBinaryEncoding(contentEncoding)) {
            return Arrays.toString(body);
        }

        MessageType messageType = MessageType.mapToMessageType(contentType, contentEncoding);
        if (messageType != null && MessageType.isBinary(messageType.name())) {
            return Arrays.toString(body);
        }

        return MessagePayloadUtils.prettyPrint(new String(body, getCharset(contentType)).trim());
    }

    private static Charset getCharset(String contentType) {
        if (contentType == null) {
            return UTF_8;
        }

        String[] contentTypeParts = contentType.split(";");
        for (String contentTypePart : contentTypeParts) {
            if (contentTypePart.startsWith("charset=") && !contentTypePart.endsWith("charset=")) {
                String charset = contentTypePart.split("=")[1];
                try {
                    return Charset.forName(charset);
                } catch (IllegalArgumentException e) {
                    return UTF_8;
                }
            }
            break;
        }

        return UTF_8;
    }

    private static boolean isBinaryEncoding(String contentEncoding) {
        if (contentEncoding == null) {
            return false;
        }

        return Stream.of("zstd", "gzip", "deflate")
                .anyMatch(encoding -> encoding.equalsIgnoreCase(contentEncoding));
    }
}
