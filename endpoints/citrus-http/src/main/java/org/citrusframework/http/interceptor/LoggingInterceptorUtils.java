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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.stream.Stream;

import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.message.MessageType;

import static java.nio.charset.StandardCharsets.UTF_8;

class LoggingInterceptorUtils {

    private LoggingInterceptorUtils() {
        // prevent instantiation of utility class
    }

    public static String getBodyContent(byte[] body, String contentType, String contentEncoding) {
        if (body == null || body.length == 0) {
            return "";
        }

        if (isBinaryEncoding(contentEncoding)) {
            return binaryContentSummary(body);
        }

        Charset charset = getCharset(contentType);
        MessageType messageType = MessageType.mapToMessageType(contentType, contentEncoding);

        if (messageType != null) {
            if (MessageType.isBinary(messageType.name())) {
                return binaryContentSummary(body);
            }

            return MessagePayloadUtils.prettyPrint(new String(body, charset).trim());
        }

        // the media type is unknown to Citrus, so the payload itself decides how it is rendered
        if (!isTextContent(body, charset)) {
            return binaryContentSummary(body);
        }

        return MessagePayloadUtils.prettyPrint(new String(body, charset).trim());
    }

    /**
     * Creates a summary of a binary body holding the number of bytes only.
     * @param body the binary payload
     * @return the summary to write to the log
     */
    private static String binaryContentSummary(byte[] body) {
        return "<binary " + body.length + " bytes>";
    }

    /**
     * Checks if the given payload is readable text. The payload must decode without error in the given charset and
     * must not contain control characters other than tab, line feed and carriage return.
     * @param body the payload to check
     * @param charset the charset used to decode the payload
     * @return true if the payload can be written to the log as text
     */
    private static boolean isTextContent(byte[] body, Charset charset) {
        String content;

        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            content = decoder.decode(ByteBuffer.wrap(body)).toString();
        } catch (CharacterCodingException e) {
            return false;
        }

        for (int i = 0; i < content.length(); i++) {
            char currentChar = content.charAt(i);

            if (currentChar == '\t' || currentChar == '\n' || currentChar == '\r') {
                continue;
            }

            if (Character.isISOControl(currentChar)) {
                return false;
            }
        }

        return true;
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
