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

import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingInterceptorUtilsTest {

    @Test
    public void shouldRenderBinaryMediaTypeAsSummary() {
        byte[] body = new byte[] { 1, 2, 3 };

        String result = LoggingInterceptorUtils.getBodyContent(body, "application/octet-stream", null);

        assertThat(result).isEqualTo("<binary 3 bytes>");
        assertThat(result).doesNotContain("1, 2, 3");
    }

    @Test
    public void shouldRenderAllBinaryMediaTypesAsSummary() {
        String[] binaryMediaTypes = new String[] {
                "application/octet-stream",
                "application/pdf",
                "application/zip",
                "application/x-7z-compressed",
                "image/png",
                "image/jpeg",
                "image/gif"
        };

        for (String mediaType : binaryMediaTypes) {
            String result = LoggingInterceptorUtils.getBodyContent("binary".getBytes(StandardCharsets.UTF_8), mediaType, null);

            assertThat(result).as("media type %s", mediaType).isEqualTo("<binary 6 bytes>");
        }
    }

    @Test
    public void shouldRenderBinaryContentEncodingAsSummary() {
        String[] binaryContentEncodings = new String[] { "gzip", "deflate", "zstd" };

        for (String contentEncoding : binaryContentEncodings) {
            String result = LoggingInterceptorUtils.getBodyContent(new byte[] { 1, 2, 3 }, "application/json", contentEncoding);

            assertThat(result).as("content encoding %s", contentEncoding).isEqualTo("<binary 3 bytes>");
        }
    }

    @Test
    public void shouldRenderUnmappedMediaTypeWithTextPayloadAsText() {
        String result = LoggingInterceptorUtils.getBodyContent("{\"id\":\"42\"}".getBytes(StandardCharsets.UTF_8),
                "application/vnd.api+json", null);

        assertThat(result).contains("42");
        assertThat(result).doesNotContain("<binary");
    }

    @Test
    public void shouldRenderUnmappedMediaTypeWithBinaryPayloadAsSummary() {
        byte[] body = new byte[] { 8, -106, 1 };

        String result = LoggingInterceptorUtils.getBodyContent(body, "application/x-protobuf", null);

        assertThat(result).isEqualTo("<binary 3 bytes>");
    }

    @Test
    public void shouldRenderMissingContentTypeWithTextPayloadAsText() {
        String result = LoggingInterceptorUtils.getBodyContent("hello".getBytes(StandardCharsets.UTF_8), null, null);

        assertThat(result).isEqualTo("hello");
    }

    @Test
    public void shouldRenderMissingContentTypeWithBinaryPayloadAsSummary() {
        byte[] body = new byte[] { 8, -106, 1 };

        String result = LoggingInterceptorUtils.getBodyContent(body, null, null);

        assertThat(result).isEqualTo("<binary 3 bytes>");
    }

    @Test
    public void shouldRenderUnmappedMediaTypeWithControlCharactersAsSummary() {
        byte[] body = "a\u0007b".getBytes(StandardCharsets.UTF_8);

        String result = LoggingInterceptorUtils.getBodyContent(body, "application/x-custom", null);

        assertThat(result).isEqualTo("<binary 3 bytes>");
    }

    @Test
    public void shouldRenderUnmappedMediaTypeWithFormFeedAsText() {
        byte[] body = "page1\fpage2".getBytes(StandardCharsets.UTF_8);

        String result = LoggingInterceptorUtils.getBodyContent(body, "application/x-custom", null);

        assertThat(result).isEqualTo("page1\fpage2");
    }

    @Test
    public void shouldRenderTextualMediaTypeAsText() {
        String jsonResult = LoggingInterceptorUtils.getBodyContent("{\"id\":\"42\"}".getBytes(StandardCharsets.UTF_8),
                "application/json", null);
        String plainResult = LoggingInterceptorUtils.getBodyContent("hello world".getBytes(StandardCharsets.UTF_8),
                "text/plain", null);
        String xmlResult = LoggingInterceptorUtils.getBodyContent("<root><child>value</child></root>".getBytes(StandardCharsets.UTF_8),
                "application/xml", null);

        assertThat(jsonResult).contains("42");
        assertThat(plainResult).isEqualTo("hello world");
        assertThat(xmlResult).contains("<child>value</child>");
    }

    @Test
    public void shouldRenderBase64TransferEncodingAsText() {
        String result = LoggingInterceptorUtils.getBodyContent("AAEC".getBytes(StandardCharsets.UTF_8),
                "application/octet-stream", "base64");

        assertThat(result).isEqualTo("AAEC");
    }

    @Test
    public void shouldRenderEmptyBinaryBodyAsZeroBytesSummary() {
        String result = LoggingInterceptorUtils.getBodyContent(new byte[0], "application/octet-stream", null);

        assertThat(result).isEqualTo("<binary 0 bytes>");
    }

    @Test
    public void shouldRenderNullBinaryBodyAsZeroBytesSummary() {
        String result = LoggingInterceptorUtils.getBodyContent(null, "application/octet-stream", null);

        assertThat(result).isEqualTo("<binary 0 bytes>");
    }

    @Test
    public void shouldRenderEmptyTextBodyAsEmptyString() {
        String result = LoggingInterceptorUtils.getBodyContent(new byte[0], "text/plain", null);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldRenderNullTextBodyAsEmptyString() {
        String result = LoggingInterceptorUtils.getBodyContent(null, "text/plain", null);

        assertThat(result).isEmpty();
    }
}
