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

package org.citrusframework.grpc.message;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.grpc.Metadata;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageHeaders;

/**
 * Maps between gRPC metadata and Citrus message headers.
 *
 * <p>Plain metadata keys map 1:1 to message headers, response trailers are exposed with the
 * {@link GrpcMessageHeaders#TRAILER_PREFIX} prefix and binary ({@code -bin}) metadata values are
 * represented as base64 encoded strings in Citrus and as raw bytes on the wire. Multiple values of
 * a binary header are separated by a comma, because the base64 alphabet does not contain commas.</p>
 *
 * @since 5.1
 */
public final class GrpcMetadataMapper {

    private static final String BINARY_HEADER_SUFFIX = "-bin";

    private GrpcMetadataMapper() {
        // prevent instantiation
    }

    /**
     * Creates request metadata from the given message headers. Citrus internal headers and
     * reserved transport keys are not written to the wire.
     *
     * @param headers the message headers
     * @return the gRPC metadata
     */
    public static Metadata toMetadata(Map<String, Object> headers) {
        Metadata metadata = new Metadata();

        headers.forEach((name, value) -> {
            if (isMetadataHeader(name)) {
                put(metadata, name, value);
            }
        });

        return metadata;
    }

    /**
     * Creates message headers from the given request metadata or response headers.
     *
     * @param metadata the gRPC metadata
     * @return the message headers
     */
    public static Map<String, Object> fromMetadata(Metadata metadata) {
        Map<String, Object> headers = new LinkedHashMap<>();
        read(metadata, headers, "");
        return headers;
    }

    /**
     * Creates response trailers from the given message headers. Only headers carrying the
     * {@link GrpcMessageHeaders#TRAILER_PREFIX} prefix are used and the prefix is removed.
     *
     * @param headers the message headers
     * @return the gRPC trailers
     */
    public static Metadata toTrailers(Map<String, Object> headers) {
        Metadata trailers = new Metadata();

        headers.forEach((name, value) -> {
            if (name != null && name.startsWith(GrpcMessageHeaders.TRAILER_PREFIX)) {
                put(trailers, name.substring(GrpcMessageHeaders.TRAILER_PREFIX.length()), value);
            }
        });

        return trailers;
    }

    /**
     * Creates message headers from the given response trailers. Every trailer is prefixed with
     * {@link GrpcMessageHeaders#TRAILER_PREFIX}.
     *
     * @param trailers the gRPC trailers
     * @return the message headers
     */
    public static Map<String, Object> fromTrailers(Metadata trailers) {
        Map<String, Object> headers = new LinkedHashMap<>();
        read(trailers, headers, GrpcMessageHeaders.TRAILER_PREFIX);
        return headers;
    }

    private static void read(Metadata metadata, Map<String, Object> headers, String prefix) {
        for (String name : metadata.keys()) {
            if (name.endsWith(BINARY_HEADER_SUFFIX)) {
                List<String> values = new ArrayList<>();
                for (byte[] value : metadata.getAll(Metadata.Key.of(name, Metadata.BINARY_BYTE_MARSHALLER))) {
                    values.add(Base64.getEncoder().encodeToString(value));
                }

                if (!values.isEmpty()) {
                    headers.put(prefix + name, String.join(",", values));
                }
            } else {
                List<String> values = new ArrayList<>();
                for (String value : metadata.getAll(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER))) {
                    values.add(value);
                }

                if (!values.isEmpty()) {
                    headers.put(prefix + name, String.join(",", values));
                }
            }
        }
    }

    private static void put(Metadata metadata, String name, Object value) {
        if (name == null || name.isBlank()) {
            throw new CitrusRuntimeException("Invalid gRPC metadata header - the header name must not be empty");
        }

        try {
            if (name.endsWith(BINARY_HEADER_SUFFIX)) {
                for (byte[] bytes : binaryValues(value)) {
                    metadata.put(Metadata.Key.of(name, Metadata.BINARY_BYTE_MARSHALLER), bytes);
                }
            } else {
                metadata.put(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER), String.valueOf(value));
            }
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Invalid gRPC metadata header '%s' - %s".formatted(name, e.getMessage()), e);
        }
    }

    /**
     * Decodes the binary values of a {@code -bin} header. Multiple values are separated by a comma.
     * The base64 alphabet does not contain commas, so the separator is unambiguous.
     *
     * @param value the header value
     * @return the decoded byte arrays
     */
    private static List<byte[]> binaryValues(Object value) {
        if (value instanceof byte[] bytes) {
            return List.of(bytes);
        }

        List<byte[]> values = new ArrayList<>();
        for (String part : String.valueOf(value).split(",")) {
            String encoded = part.trim();
            if (!encoded.isEmpty()) {
                values.add(Base64.getDecoder().decode(encoded));
            }
        }

        return values;
    }

    private static boolean isMetadataHeader(String name) {
        return name != null
                && !name.startsWith(MessageHeaders.PREFIX)
                && !isReservedTransportKey(name);
    }

    private static boolean isReservedTransportKey(String name) {
        return name.startsWith(":")
                || name.equalsIgnoreCase("content-type")
                || name.startsWith("grpc-")
                || name.equals("te")
                || name.equals("user-agent");
    }
}
