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

package org.citrusframework.http.vertx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;

/** Explicit inbound payload type and Spring body converter for the test-only server. */
public record VertxMessageBodyConverter<T>(Class<T> payloadType, HttpMessageConverter<? super T> converter) {
    public VertxMessageBodyConverter {
        Objects.requireNonNull(payloadType, "payloadType");
        Objects.requireNonNull(converter, "converter");
    }

    Object read(HttpHeaders headers, byte[] body) {
        try {
            return converter.read(payloadType, new HttpInputMessage() {
                @Override public InputStream getBody() {
                    return new ByteArrayInputStream(body == null ? new byte[0] : body);
                }
                @Override public HttpHeaders getHeaders() { return headers; }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Custom request body conversion failed", e);
        }
    }

    byte[] write(Object body, Map<String, List<String>> wireHeaders) {
        var headers = new HttpHeaders();
        wireHeaders.forEach((name, values) -> headers.put(name, new ArrayList<>(values)));
        var bytes = new ByteArrayOutputStream();
        try {
            converter.write(payloadType.cast(body), headers.getContentType(), new HttpOutputMessage() {
                @Override public OutputStream getBody() { return bytes; }
                @Override public HttpHeaders getHeaders() { return headers; }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Custom response body conversion failed", e);
        }
        wireHeaders.clear();
        headers.headerSet().forEach(entry -> wireHeaders.put(entry.getKey(), new ArrayList<>(entry.getValue())));
        return bytes.toByteArray();
    }
}
