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
import java.util.Optional;
import java.util.stream.Stream;

import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.message.MessageType;

import static java.nio.charset.StandardCharsets.UTF_8;

class LoggingInterceptorUtils {

    private LoggingInterceptorUtils() {
        // prevent instantiation of utility class
    }

    public static String getBodyContent(byte[] body, String contentType, String contentEncoding) {
        byte[] payload = body != null ? body : new byte[0];

        if (isBinaryEncoding(contentEncoding)) {
            return binaryContentSummary(payload);
        }

        MessageType messageType = MessageType.mapToMessageType(contentType, contentEncoding);
        if (messageType != null && MessageType.isBinary(messageType.name())) {
            return binaryContentSummary(payload);
        }

        if (payload.length == 0) {
            return "";
        }

        Charset charset = getCharset(contentType);
        if (messageType != null) {
            return MessagePayloadUtils.prettyPrint(new String(payload, charset).trim());
        }

        // the media type is unknown to Citrus, so the payload itself decides how it is rendered
        return decodeTextContent(payload, charset)
                .map(content -> MessagePayloadUtils.prettyPrint(content.trim()))
                .orElseGet(() -> binaryContentSummary(payload));
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
     * Decodes the given payload when it is readable text. The payload must decode without error in the given charset
     * and must not contain control characters other than standard whitespace.
     * @param body the payload to decode
     * @param charset the charset used to decode the payload
     * @return the decoded text content if the payload can be written to the log as text, or empty otherwise
     */
    private static Optional<String> decodeTextContent(byte[] body, Charset charset) {
        String content;

        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            content = decoder.decode(ByteBuffer.wrap(body)).toString();
        } catch (CharacterCodingException e) {
            return Optional.empty();
        }

        for (int i = 0; i < content.length(); i++) {
            char currentChar = content.charAt(i);

            if (Character.isWhitespace(currentChar)) {
                continue;
            }

            if (Character.isISOControl(currentChar)) {
                return Optional.empty();
            }
        }

        return Optional.of(content);
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
