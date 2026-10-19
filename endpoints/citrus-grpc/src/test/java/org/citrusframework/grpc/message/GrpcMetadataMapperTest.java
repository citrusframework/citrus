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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import io.grpc.Metadata;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcMetadataMapperTest {

    @Test
    public void shouldMapHeadersToMetadata() {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("authorization", "Bearer abc");
        headers.put("x-request-id", 42);
        headers.put("citrus_message_id", "internal");
        headers.put(GrpcMessageHeaders.METHOD, "helloworld.Greeter/SayHello");
        headers.put(GrpcMessageHeaders.TRAILER_PREFIX + "x-trailer", "ignored");

        Metadata metadata = GrpcMetadataMapper.toMetadata(headers);

        assertThat(metadata.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)))
                .isEqualTo("Bearer abc");
        assertThat(metadata.get(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)))
                .isEqualTo("42");
        assertThat(metadata.keys()).containsExactlyInAnyOrder("authorization", "x-request-id");
    }

    @Test
    public void shouldSkipReservedTransportKeys() {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(":path", "/helloworld.Greeter/SayHello");
        headers.put("content-type", "application/grpc");
        headers.put("grpc-timeout", "1S");
        headers.put("te", "trailers");
        headers.put("user-agent", "grpc-java-netty");
        headers.put("x-request-id", "42");

        Metadata metadata = GrpcMetadataMapper.toMetadata(headers);

        assertThat(metadata.keys()).containsExactly("x-request-id");
    }

    @Test
    public void shouldMapBinaryHeadersToMetadata() {
        byte[] payload = new byte[] { 1, 2, 3 };
        Map<String, Object> headers = Map.of("trace-bin", Base64.getEncoder().encodeToString(payload));

        Metadata metadata = GrpcMetadataMapper.toMetadata(headers);

        assertThat(metadata.get(Metadata.Key.of("trace-bin", Metadata.BINARY_BYTE_MARSHALLER)))
                .isEqualTo(payload);
    }

    @Test
    public void shouldMapMetadataToHeaders() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer abc");
        byte[] payload = new byte[] { 1, 2, 3 };
        metadata.put(Metadata.Key.of("trace-bin", Metadata.BINARY_BYTE_MARSHALLER), payload);

        Map<String, Object> headers = GrpcMetadataMapper.fromMetadata(metadata);

        assertThat(headers).containsEntry("authorization", "Bearer abc");
        assertThat(headers).containsEntry("trace-bin", Base64.getEncoder().encodeToString(payload));
    }

    @Test
    public void shouldMapTrailersBothWays() {
        Map<String, Object> headers = Map.of(
                GrpcMessageHeaders.TRAILER_PREFIX + "x-request-id", "42",
                "x-request-id", "not-a-trailer");

        Metadata trailers = GrpcMetadataMapper.toTrailers(headers);

        assertThat(trailers.keys()).containsExactly("x-request-id");
        assertThat(trailers.get(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)))
                .isEqualTo("42");

        Map<String, Object> mapped = GrpcMetadataMapper.fromTrailers(trailers);
        assertThat(mapped).containsEntry(GrpcMessageHeaders.TRAILER_PREFIX + "x-request-id", "42");
    }

    @Test
    public void shouldConvertAsciiValuesToText() {
        byte[] value = "raw".getBytes(StandardCharsets.UTF_8);
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("x-text", Metadata.ASCII_STRING_MARSHALLER), new String(value, StandardCharsets.UTF_8));

        assertThat(GrpcMetadataMapper.fromMetadata(metadata)).containsEntry("x-text", "raw");
    }
}
